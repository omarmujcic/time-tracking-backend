package com.omarmujcic.timetracking.core.notifications.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.auth.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification_push_subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class NotificationPushSubscription {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "text")
    private String endpoint;

    @Column(name = "p256dh_key", nullable = false, columnDefinition = "text")
    private String p256dhKey;

    @Column(name = "auth_key", nullable = false, columnDefinition = "text")
    private String authKey;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "last_failed_at")
    private OffsetDateTime lastFailedAt;
}
