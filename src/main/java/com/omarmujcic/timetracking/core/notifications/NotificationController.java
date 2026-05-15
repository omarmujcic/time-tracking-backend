package com.omarmujcic.timetracking.core.notifications;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.notifications.dto.CreateProjectBillingIssueRequestDTO;
import com.omarmujcic.timetracking.core.notifications.dto.NotificationCountDTO;
import com.omarmujcic.timetracking.core.notifications.dto.NotificationDTO;
import com.omarmujcic.timetracking.core.notifications.dto.NotificationStatusFilter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDTO> list(@AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "OPEN") NotificationStatusFilter status) {
        return notificationService.list(user, status);
    }

    @GetMapping("/open-count")
    public NotificationCountDTO openCount(@AuthenticationPrincipal User user) {
        return new NotificationCountDTO(notificationService.openCount(user));
    }

    @PostMapping("/project-billing-issues")
    public NotificationDTO createProjectBillingIssue(@AuthenticationPrincipal User user,
            @Valid @RequestBody CreateProjectBillingIssueRequestDTO request) {
        return notificationService.createProjectBillingIssue(user, request);
    }

    @PostMapping("/{id}/resolve")
    public NotificationDTO resolve(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return notificationService.resolve(user, id);
    }

    @PostMapping("/{id}/reopen")
    public NotificationDTO reopen(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return notificationService.reopen(user, id);
    }

    @PostMapping("/{id}/dismiss")
    public void dismiss(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        notificationService.dismiss(user, id);
    }
}
