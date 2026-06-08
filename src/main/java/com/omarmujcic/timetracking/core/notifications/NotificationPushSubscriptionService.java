package com.omarmujcic.timetracking.core.notifications;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.notifications.dto.PushPublicKeyDTO;
import com.omarmujcic.timetracking.core.notifications.dto.PushSubscriptionRequestDTO;
import com.omarmujcic.timetracking.core.notifications.mapper.NotificationPushSubscriptionMapper;
import com.omarmujcic.timetracking.core.notifications.repository.NotificationPushSubscriptionRepository;
import com.omarmujcic.timetracking.core.settings.repository.UserPreferenceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPushSubscriptionService {

    private final NotificationPushSubscriptionRepository subscriptionRepository;
    private final NotificationPushSubscriptionMapper subscriptionMapper;
    private final UserPreferenceRepository preferenceRepository;

    @Value("${app.notifications.push.public-key:}")
    private String publicKey;

    @Transactional(readOnly = true)
    public PushPublicKeyDTO publicKey() {
        String trimmed = publicKey == null ? "" : publicKey.trim();
        return new PushPublicKeyDTO(trimmed, !trimmed.isBlank());
    }

    @Transactional
    public void register(User user, PushSubscriptionRequestDTO request) {
        OffsetDateTime now = now();
        subscriptionRepository.findByEndpoint(request.getEndpoint().trim())
            .ifPresentOrElse(
                    subscription -> {
                        UUID previousUserId = subscription.getUser().getId();
                        subscriptionMapper.updateEntity(request, user, now, subscription);
                        updatePreviousOwnerPreference(previousUserId, user.getId(), now);
                    },
                    () -> subscriptionRepository.save(subscriptionMapper.toEntity(request, user, now))
            );
        updatePushPreference(user, true, now);
    }

    @Transactional
    public void unsubscribe(User user, PushSubscriptionRequestDTO request) {
        subscriptionRepository.findByEndpoint(request.getEndpoint().trim())
            .ifPresent(subscription -> {
                UUID ownerId = subscription.getUser().getId();
                subscriptionRepository.deleteByEndpoint(subscription.getEndpoint());
                updateOwnerPreference(ownerId, now());
            });
        updatePushPreference(user, subscriptionRepository.existsByUserId(user.getId()), now());
    }

    @Transactional
    public void disableAll(User user) {
        subscriptionRepository.deleteByUserId(user.getId());
        updatePushPreference(user, false, now());
    }

    private void updatePushPreference(User user, boolean enabled, OffsetDateTime now) {
        preferenceRepository.findById(user.getId()).ifPresent((preference) -> {
            preference.setBrowserPushEnabled(enabled);
            preference.setUpdatedAt(now);
        });
    }

    private void updatePreviousOwnerPreference(UUID previousUserId, UUID currentUserId, OffsetDateTime now) {
        if (!previousUserId.equals(currentUserId)) {
            updateOwnerPreference(previousUserId, now);
        }
    }

    private void updateOwnerPreference(UUID userId, OffsetDateTime now) {
        preferenceRepository.findById(userId).ifPresent((preference) -> {
            preference.setBrowserPushEnabled(subscriptionRepository.existsByUserId(userId));
            preference.setUpdatedAt(now);
        });
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
