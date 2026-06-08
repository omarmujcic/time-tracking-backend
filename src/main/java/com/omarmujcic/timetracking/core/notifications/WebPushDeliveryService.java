package com.omarmujcic.timetracking.core.notifications;

import java.security.Security;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.notifications.entity.Notification;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationPushSubscription;
import com.omarmujcic.timetracking.core.notifications.mapper.NotificationPushSubscriptionMapper;
import com.omarmujcic.timetracking.core.notifications.repository.NotificationPushSubscriptionRepository;
import com.omarmujcic.timetracking.core.settings.repository.UserPreferenceRepository;

import nl.martijndwars.webpush.PushService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebPushDeliveryService {

    private final NotificationPushSubscriptionRepository subscriptionRepository;
    private final NotificationPushSubscriptionMapper subscriptionMapper;
    private final UserPreferenceRepository preferenceRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.notifications.push.public-key:}")
    private String publicKey;

    @Value("${app.notifications.push.private-key:}")
    private String privateKey;

    @Value("${app.notifications.push.subject:mailto:noreply@trackz.local}")
    private String subject;

    public void send(User user, Notification notification) {
        if (!configured() || !pushEnabled(user)) {
            return;
        }

        List<NotificationPushSubscription> subscriptions = subscriptionRepository.findByUserId(user.getId());
        if (subscriptions.isEmpty()) {
            return;
        }

        String payload = payload(notification);
        if (payload == null) {
            return;
        }

        PushService pushService;
        try {
            Security.addProvider(new BouncyCastleProvider());
            pushService = new PushService(publicKey.trim(), privateKey.trim(), subject.trim());
        } catch (Exception exception) {
            return;
        }

        for (NotificationPushSubscription subscription : subscriptions) {
            try {
                pushService.send(new nl.martijndwars.webpush.Notification(
                        subscription.getEndpoint(),
                        subscription.getP256dhKey(),
                        subscription.getAuthKey(),
                        payload
                ));
            } catch (Exception exception) {
                subscriptionMapper.markFailed(now(), subscription);
            }
        }
    }

    private boolean configured() {
        return publicKey != null && !publicKey.isBlank()
            && privateKey != null && !privateKey.isBlank()
            && subject != null && !subject.isBlank();
    }

    private boolean pushEnabled(User user) {
        return preferenceRepository.findById(user.getId())
            .map(preference -> preference.isBrowserPushEnabled())
            .orElse(false);
    }

    private String payload(Notification notification) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            String route = sourceRoute(notification);
            data.put("url", route);
            data.put("workspaceType", notification.getWorkspaceType().name());
            if (notification.getOrganization() != null) {
                data.put("organizationId", notification.getOrganization().getId().toString());
            }
            data.put("onActionClick", Map.of(
                    "default", Map.of(
                            "operation", "focusLastFocusedOrOpen",
                            "url", route
                    )
            ));
            return objectMapper.writeValueAsString(Map.of(
                    "notification", Map.of(
                            "title", title(notification),
                            "body", notification.getMessage(),
                            "icon", "/favicon.ico",
                            "badge", "/favicon.ico",
                            "data", data
                    )
            ));
        } catch (JacksonException exception) {
            return null;
        }
    }

    private String title(Notification notification) {
        return switch (notification.getType()) {
            case LONG_RUNNING_TIMER -> "Timer still running";
            case MISSING_DAILY_TIME -> "No time tracked today";
            case INVOICE_PERIOD_REMINDER -> "Invoice period ready";
            case PROJECT_BILLING_ISSUE -> "Project billing issue";
        };
    }

    private String sourceRoute(Notification notification) {
        String route = notification.getSourceRoute() == null || notification.getSourceRoute().isBlank()
                ? "/time-trackz/notifications"
                : notification.getSourceRoute();
        String[] fragmentParts = route.split("#", 2);
        String beforeHash = fragmentParts[0];
        String fragment = fragmentParts.length > 1 ? "#" + fragmentParts[1] : "";
        String[] queryParts = beforeHash.split("\\?", 2);
        String path = queryParts[0];
        String query = queryParts.length > 1 ? queryParts[1] : "";
        List<String> params = new ArrayList<>();
        if (!query.isBlank()) {
            for (String param : query.split("&")) {
                String key = param.split("=", 2)[0];
                if (!param.isBlank() && !"workspaceType".equals(key) && !"organizationId".equals(key)) {
                    params.add(param);
                }
            }
        }
        params.add("workspaceType=" + notification.getWorkspaceType().name());
        if (notification.getOrganization() != null) {
            params.add("organizationId=" + notification.getOrganization().getId());
        }
        return path + "?" + String.join("&", params) + fragment;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
