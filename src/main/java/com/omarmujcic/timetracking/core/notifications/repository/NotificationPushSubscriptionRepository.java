package com.omarmujcic.timetracking.core.notifications.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.notifications.entity.NotificationPushSubscription;

public interface NotificationPushSubscriptionRepository extends JpaRepository<NotificationPushSubscription, UUID> {
    List<NotificationPushSubscription> findByUserId(UUID userId);

    Optional<NotificationPushSubscription> findByEndpoint(String endpoint);

    void deleteByEndpoint(String endpoint);

    void deleteByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
