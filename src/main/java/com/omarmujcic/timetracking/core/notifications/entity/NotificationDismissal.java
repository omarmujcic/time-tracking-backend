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
@Table(name = "notification_dismissals")
@Getter
@Setter
@NoArgsConstructor
public class NotificationDismissal {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "dismissed_at", nullable = false)
    private OffsetDateTime dismissedAt;
}
