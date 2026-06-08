package com.omarmujcic.timetracking.core.notifications.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.omarmujcic.timetracking.core.notifications.entity.Notification;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationStatus;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("""
        select notification
        from Notification notification
        where notification.workspaceType = :workspaceType
          and notification.workspaceUser.id = :userId
          and (:status is null or notification.status = :status)
          and not exists (
              select dismissal.id
              from NotificationDismissal dismissal
              where dismissal.notification.id = notification.id
                and dismissal.user.id = :userId
          )
        order by notification.createdAt desc
        """)
    List<Notification> findVisiblePersonalNotifications(
            @Param("workspaceType") WorkspaceType workspaceType,
            @Param("userId") UUID userId,
            @Param("status") NotificationStatus status);

    @Query("""
        select count(notification)
        from Notification notification
        where notification.workspaceType = :workspaceType
          and notification.workspaceUser.id = :userId
          and notification.status = :status
          and not exists (
              select dismissal.id
              from NotificationDismissal dismissal
              where dismissal.notification.id = notification.id
                and dismissal.user.id = :userId
          )
        """)
    long countVisiblePersonalNotifications(
            @Param("workspaceType") WorkspaceType workspaceType,
            @Param("userId") UUID userId,
            @Param("status") NotificationStatus status);

    @Query("""
        select notification
        from Notification notification
        where notification.workspaceType = :workspaceType
          and notification.organization.id = :organizationId
          and (
              (notification.recipientUser is null and (:manager = true or notification.createdBy.id = :userId))
              or notification.recipientUser.id = :userId
          )
          and (:status is null or notification.status = :status)
          and not exists (
              select dismissal.id
              from NotificationDismissal dismissal
              where dismissal.notification.id = notification.id
                and dismissal.user.id = :userId
          )
        order by notification.createdAt desc
        """)
    List<Notification> findVisibleOrganizationNotifications(
            @Param("workspaceType") WorkspaceType workspaceType,
            @Param("organizationId") UUID organizationId,
            @Param("userId") UUID userId,
            @Param("manager") boolean manager,
            @Param("status") NotificationStatus status);

    @Query("""
        select count(notification)
        from Notification notification
        where notification.workspaceType = :workspaceType
          and notification.organization.id = :organizationId
          and notification.status = :status
          and (
              (notification.recipientUser is null and (:manager = true or notification.createdBy.id = :userId))
              or notification.recipientUser.id = :userId
          )
          and not exists (
              select dismissal.id
              from NotificationDismissal dismissal
              where dismissal.notification.id = notification.id
                and dismissal.user.id = :userId
          )
        """)
    long countVisibleOrganizationNotifications(
            @Param("workspaceType") WorkspaceType workspaceType,
            @Param("organizationId") UUID organizationId,
            @Param("userId") UUID userId,
            @Param("manager") boolean manager,
            @Param("status") NotificationStatus status);

    @Query("""
        select count(notification)
        from Notification notification
        where notification.workspaceType = :workspaceType
          and notification.organization.id = :organizationId
          and notification.status = :status
          and notification.createdBy.id = :userId
          and notification.resolvedBy is not null
          and notification.resolvedBy.id <> :userId
          and not exists (
              select dismissal.id
              from NotificationDismissal dismissal
              where dismissal.notification.id = notification.id
                and dismissal.user.id = :userId
          )
        """)
    long countResolvedByOtherUserCreatorNotifications(
            @Param("workspaceType") WorkspaceType workspaceType,
            @Param("organizationId") UUID organizationId,
            @Param("userId") UUID userId,
            @Param("status") NotificationStatus status);

    Optional<Notification> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<Notification> findByIdAndWorkspaceUserId(UUID id, UUID userId);

    boolean existsByReminderKey(String reminderKey);
}
