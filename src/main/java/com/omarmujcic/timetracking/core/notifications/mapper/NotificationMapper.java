package com.omarmujcic.timetracking.core.notifications.mapper;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.notifications.dto.NotificationDTO;
import com.omarmujcic.timetracking.core.notifications.dto.ProjectBillingIssueNotificationDTO;
import com.omarmujcic.timetracking.core.notifications.entity.Notification;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationDismissal;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationStatus;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationType;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

@Mapper(componentModel = "spring", imports = {
        UUID.class,
        WorkspaceType.class,
        NotificationType.class,
        NotificationStatus.class
})
public interface NotificationMapper {

    @Mapping(target = "createdByUserId", source = "notification.createdBy.id")
    @Mapping(target = "createdByUsername", source = "notification.createdBy.username")
    @Mapping(target = "createdByDisplayName", source = "notification.createdBy.displayName")
    @Mapping(target = "resolvedByUserId", source = "notification.resolvedBy.id")
    @Mapping(target = "resolvedByUsername", source = "notification.resolvedBy.username")
    @Mapping(target = "resolvedByDisplayName", source = "notification.resolvedBy.displayName")
    @Mapping(target = "canResolve", source = "canResolve")
    @Mapping(target = "canReopen", source = "canReopen")
    @Mapping(target = "canDismiss", source = "canDismiss")
    NotificationDTO toDTO(Notification notification, boolean canResolve, boolean canReopen, boolean canDismiss);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "workspaceType", expression = "java(WorkspaceType.ORGANIZATION)")
    @Mapping(target = "workspaceUser", ignore = true)
    @Mapping(target = "organization", source = "notification.organization")
    @Mapping(target = "createdBy", source = "notification.creator")
    @Mapping(target = "type", expression = "java(NotificationType.PROJECT_BILLING_ISSUE)")
    @Mapping(target = "status", expression = "java(NotificationStatus.OPEN)")
    @Mapping(target = "message", source = "notification.message")
    @Mapping(target = "subjectType", constant = "PROJECT")
    @Mapping(target = "subjectId", source = "notification.projectId")
    @Mapping(target = "subjectLabel", source = "notification.projectName")
    @Mapping(target = "createdAt", source = "notification.createdAt")
    @Mapping(target = "updatedAt", source = "notification.createdAt")
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "resolvedBy", ignore = true)
    Notification toProjectBillingIssue(ProjectBillingIssueNotificationDTO notification);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "status", expression = "java(NotificationStatus.RESOLVED)")
    @Mapping(target = "resolvedAt", source = "now")
    @Mapping(target = "resolvedBy", source = "resolvedBy")
    @Mapping(target = "updatedAt", source = "now")
    void resolve(OffsetDateTime now, User resolvedBy, @MappingTarget Notification notification);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "status", expression = "java(NotificationStatus.OPEN)")
    @Mapping(target = "resolvedAt", expression = "java(null)")
    @Mapping(target = "resolvedBy", expression = "java(null)")
    @Mapping(target = "updatedAt", source = "now")
    void reopen(OffsetDateTime now, @MappingTarget Notification notification);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "notification", source = "notification")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "dismissedAt", source = "now")
    NotificationDismissal toDismissal(Notification notification, User user, OffsetDateTime now);
}
