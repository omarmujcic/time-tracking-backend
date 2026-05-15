package com.omarmujcic.timetracking.core.notifications.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.notifications.entity.NotificationDismissal;

public interface NotificationDismissalRepository extends JpaRepository<NotificationDismissal, UUID> {
    boolean existsByNotificationIdAndUserId(UUID notificationId, UUID userId);
}
