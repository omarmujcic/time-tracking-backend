package com.omarmujcic.timetracking.core.notifications;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.notifications.dto.CreateProjectBillingIssueRequestDTO;
import com.omarmujcic.timetracking.core.notifications.dto.NotificationDTO;
import com.omarmujcic.timetracking.core.notifications.dto.NotificationStatusFilter;
import com.omarmujcic.timetracking.core.notifications.dto.ProjectBillingIssueNotificationDTO;
import com.omarmujcic.timetracking.core.notifications.dto.ReminderNotificationDTO;
import com.omarmujcic.timetracking.core.notifications.entity.Notification;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationStatus;
import com.omarmujcic.timetracking.core.notifications.mapper.NotificationMapper;
import com.omarmujcic.timetracking.core.notifications.repository.NotificationDismissalRepository;
import com.omarmujcic.timetracking.core.notifications.repository.NotificationRepository;
import com.omarmujcic.timetracking.core.projects.ProjectService;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.workspace.WorkspaceService;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDismissalRepository dismissalRepository;
    private final NotificationMapper notificationMapper;
    private final WorkspaceService workspaceService;
    private final ProjectService projectService;
    private final WebPushDeliveryService webPushDeliveryService;

    @Transactional(readOnly = true)
    public List<NotificationDTO> list(User user, NotificationStatusFilter filter) {
        if (user.getActiveWorkspaceType() == WorkspaceType.PERSONAL) {
            NotificationStatus status = statusFilter(filter);
            return notificationRepository.findVisiblePersonalNotifications(
                    WorkspaceType.PERSONAL,
                    user.getId(),
                    status
            )
                .stream()
                .sorted(notificationComparator(filter))
                .map(notification -> toDTO(notification, user, false))
                .toList();
        }
        OrganizationMember member = workspaceService.activeOrganizationMembership(user);
        boolean manager = workspaceService.canManage(member.getRole());
        NotificationStatus status = statusFilter(filter);
        return notificationRepository.findVisibleOrganizationNotifications(
                WorkspaceType.ORGANIZATION,
                member.getOrganization().getId(),
                member.getUser().getId(),
                manager,
                status
        )
            .stream()
            .sorted(notificationComparator(filter))
            .map(notification -> toDTO(notification, member, manager))
            .toList();
    }

    @Transactional(readOnly = true)
    public long openCount(User user) {
        if (user.getActiveWorkspaceType() == WorkspaceType.PERSONAL) {
            return notificationRepository.countVisiblePersonalNotifications(
                    WorkspaceType.PERSONAL,
                    user.getId(),
                    NotificationStatus.OPEN
            );
        }
        OrganizationMember member = workspaceService.activeOrganizationMembership(user);
        boolean manager = workspaceService.canManage(member.getRole());
        long visibleOpenCount = notificationRepository.countVisibleOrganizationNotifications(
                WorkspaceType.ORGANIZATION,
                member.getOrganization().getId(),
                member.getUser().getId(),
                manager,
                NotificationStatus.OPEN
        );
        if (!manager) {
            return visibleOpenCount + notificationRepository.countResolvedByOtherUserCreatorNotifications(
                    WorkspaceType.ORGANIZATION,
                    member.getOrganization().getId(),
                    member.getUser().getId(),
                    NotificationStatus.RESOLVED
            );
        }
        return visibleOpenCount;
    }

    @Transactional
    public NotificationDTO createProjectBillingIssue(User user, CreateProjectBillingIssueRequestDTO request) {
        OrganizationMember member = requireOrganizationWorkspace(user);
        String message = trimMessage(request.getMessage());
        Project project = projectService.findAccessibleProject(user, request.getProjectId());
        if (project.getOrganization() == null
                || !project.getOrganization().getId().equals(member.getOrganization().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        Notification notification = notificationMapper.toProjectBillingIssue(new ProjectBillingIssueNotificationDTO(
                message,
                member.getOrganization(),
                member.getUser(),
                project.getId(),
                project.getName(),
                now()
        ));
        Notification saved = notificationRepository.save(notification);
        return toDTO(saved, member, workspaceService.canManage(member.getRole()));
    }

    @Transactional
    public void createReminder(ReminderNotificationDTO reminder) {
        if (notificationRepository.existsByReminderKey(reminder.reminderKey())) {
            return;
        }
        Notification notification = notificationMapper.toReminder(reminder);
        Notification saved = notificationRepository.save(notification);
        webPushDeliveryService.send(reminder.recipientUser(), saved);
    }

    @Transactional
    public NotificationDTO resolve(User user, UUID id) {
        if (user.getActiveWorkspaceType() == WorkspaceType.PERSONAL) {
            Notification notification = findPersonalNotification(user, id);
            if (notification.getStatus() != NotificationStatus.RESOLVED) {
                notificationMapper.resolve(now(), user, notification);
            }
            return toDTO(notification, user, false);
        }
        OrganizationMember member = requireOrganizationWorkspace(user);
        Notification notification = findActionableNotification(member, id);
        assertCanChangeState(member, notification);
        if (notification.getStatus() != NotificationStatus.RESOLVED) {
            notificationMapper.resolve(now(), member.getUser(), notification);
        }
        return toDTO(notification, member, workspaceService.canManage(member.getRole()));
    }

    @Transactional
    public NotificationDTO reopen(User user, UUID id) {
        if (user.getActiveWorkspaceType() == WorkspaceType.PERSONAL) {
            Notification notification = findPersonalNotification(user, id);
            if (notification.getStatus() != NotificationStatus.OPEN) {
                notificationMapper.reopen(now(), notification);
            }
            return toDTO(notification, user, false);
        }
        OrganizationMember member = requireOrganizationWorkspace(user);
        Notification notification = findActionableNotification(member, id);
        assertCanChangeState(member, notification);
        if (notification.getStatus() != NotificationStatus.OPEN) {
            notificationMapper.reopen(now(), notification);
        }
        return toDTO(notification, member, workspaceService.canManage(member.getRole()));
    }

    @Transactional
    public void dismiss(User user, UUID id) {
        if (user.getActiveWorkspaceType() == WorkspaceType.PERSONAL) {
            Notification notification = findPersonalNotification(user, id);
            if (dismissalRepository.existsByNotificationIdAndUserId(notification.getId(), user.getId())) {
                return;
            }
            dismissalRepository.save(notificationMapper.toDismissal(notification, user, now()));
            return;
        }
        OrganizationMember member = requireOrganizationWorkspace(user);
        Notification notification = findVisibleNotification(member, id);
        if (dismissalRepository.existsByNotificationIdAndUserId(notification.getId(), member.getUser().getId())) {
            return;
        }
        dismissalRepository.save(notificationMapper.toDismissal(notification, member.getUser(), now()));
    }

    private OrganizationMember requireOrganizationWorkspace(User user) {
        if (user.getActiveWorkspaceType() == WorkspaceType.PERSONAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notifications are available for organization workspaces");
        }
        return workspaceService.activeOrganizationMembership(user);
    }

    private Notification findVisibleNotification(OrganizationMember member, UUID id) {
        Notification notification = findOrganizationNotification(member, id);
        assertCanView(member, notification);
        return notification;
    }

    private Notification findActionableNotification(OrganizationMember member, UUID id) {
        Notification notification = findOrganizationNotification(member, id);
        assertCanView(member, notification);
        return notification;
    }

    private Notification findOrganizationNotification(OrganizationMember member, UUID id) {
        return notificationRepository.findByIdAndOrganizationId(id, member.getOrganization().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    }

    private Notification findPersonalNotification(User user, UUID id) {
        return notificationRepository.findByIdAndWorkspaceUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    }

    private void assertCanChangeState(OrganizationMember member, Notification notification) {
        if (!canChangeState(member, notification)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notification access denied");
        }
    }

    private void assertCanView(OrganizationMember member, Notification notification) {
        if (!canChangeState(member, notification)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found");
        }
    }

    private boolean canChangeState(OrganizationMember member, Notification notification) {
        return workspaceService.canManage(member.getRole()) || createdByCurrentUser(member, notification)
            || recipientIsCurrentUser(member.getUser(), notification);
    }

    private boolean createdByCurrentUser(OrganizationMember member, Notification notification) {
        return notification.getCreatedBy().getId().equals(member.getUser().getId());
    }

    private boolean recipientIsCurrentUser(User user, Notification notification) {
        return notification.getRecipientUser() != null && notification.getRecipientUser().getId().equals(user.getId());
    }

    private NotificationDTO toDTO(Notification notification, OrganizationMember member, boolean manager) {
        boolean canChangeState = manager || createdByCurrentUser(member, notification)
            || recipientIsCurrentUser(member.getUser(), notification);
        return notificationMapper.toDTO(
                notification,
                notification.getStatus() == NotificationStatus.OPEN && canChangeState,
                notification.getStatus() == NotificationStatus.RESOLVED && canChangeState,
                true
        );
    }

    private NotificationDTO toDTO(Notification notification, User user, boolean manager) {
        boolean canChangeState = manager
            || notification.getCreatedBy().getId().equals(user.getId())
            || recipientIsCurrentUser(user, notification);
        return notificationMapper.toDTO(
                notification,
                notification.getStatus() == NotificationStatus.OPEN && canChangeState,
                notification.getStatus() == NotificationStatus.RESOLVED && canChangeState,
                true
        );
    }

    private NotificationStatus statusFilter(NotificationStatusFilter filter) {
        if (filter == null || filter == NotificationStatusFilter.OPEN) {
            return NotificationStatus.OPEN;
        }
        if (filter == NotificationStatusFilter.RESOLVED) {
            return NotificationStatus.RESOLVED;
        }
        return null;
    }

    private Comparator<Notification> notificationComparator(NotificationStatusFilter filter) {
        if (filter != NotificationStatusFilter.ALL) {
            return Comparator.comparing(Notification::getCreatedAt).reversed();
        }
        return Comparator
            .comparing((Notification notification) -> notification.getStatus() == NotificationStatus.OPEN ? 0 : 1)
            .thenComparing(Notification::getCreatedAt, Comparator.reverseOrder());
    }

    private String trimMessage(String message) {
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is required");
        }
        if (trimmed.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is too long");
        }
        return trimmed;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
