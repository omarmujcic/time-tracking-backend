package com.omarmujcic.timetracking.core.notifications;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.invoices.repository.InvoiceRepository;
import com.omarmujcic.timetracking.core.notifications.dto.ReminderNotificationDTO;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationType;
import com.omarmujcic.timetracking.core.settings.entity.UserPreference;
import com.omarmujcic.timetracking.core.settings.repository.UserPreferenceRepository;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;
import com.omarmujcic.timetracking.core.timetracking.repository.TimeEntryRepository;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;
import com.omarmujcic.timetracking.core.workspace.repository.OrganizationMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReminderSchedulerService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final NotificationService notificationService;

    @Value("${app.notifications.reminders.long-timer-hours:8}")
    private long longTimerHours;

    @Value("${app.notifications.reminders.missing-daily-time-hour:18}")
    private int missingDailyTimeHour;

    @Value("${app.notifications.reminders.email-escalation-hours:12}")
    private long emailEscalationHours;

    @Scheduled(
            fixedDelayString = "${app.notifications.reminders.scan-delay-ms:300000}",
            initialDelayString = "${app.notifications.reminders.initial-delay-ms:60000}"
    )
    @Transactional
    public void scan() {
        OffsetDateTime now = now();
        createLongTimerReminders(now);
        createMissingDailyTimeReminders(now);
        createInvoicePeriodReminders(now);
    }

    private void createLongTimerReminders(OffsetDateTime now) {
        OffsetDateTime startedBefore = now.minusHours(Math.max(1, longTimerHours));
        for (TimeEntry entry : timeEntryRepository.findActiveTimersStartedBefore(startedBefore)) {
            UserPreference preference = preferenceRepository.findById(entry.getUser().getId()).orElse(null);
            if (preference == null || !preference.isLongTimerRemindersEnabled()) {
                continue;
            }
            String project = projectLabel(entry);
            notificationService.createReminder(new ReminderNotificationDTO(
                    workspaceType(entry),
                    workspaceUser(entry),
                    entry.getOrganization(),
                    entry.getUser(),
                    NotificationType.LONG_RUNNING_TIMER,
                    ReminderNotificationText.longTimerMessage(project, longTimerHours),
                    ReminderNotificationText.SUBJECT_TYPE_TIME_ENTRY,
                    entry.getId(),
                    project,
                    ReminderNotificationText.SOURCE_ROUTE_TIMER,
                    ReminderNotificationText.SOURCE_LABEL_OPEN_TIMER,
                    ReminderNotificationText.longTimerReminderKey(entry.getId()),
                    now,
                    now.plusHours(emailEscalationHours)
            ));
        }
    }

    private void createMissingDailyTimeReminders(OffsetDateTime now) {
        for (UserPreference preference : preferenceRepository.findAll()) {
            if (!preference.isMissingDailyTimeRemindersEnabled()) {
                continue;
            }
            ZoneId zone = zone(preference.getTimezone());
            LocalDate today = now.atZoneSameInstant(zone).toLocalDate();
            int hour = now.atZoneSameInstant(zone).getHour();
            if (today.getDayOfWeek().getValue() > 5 || hour < missingDailyTimeHour) {
                continue;
            }
            OffsetDateTime start = today.atStartOfDay(zone).toOffsetDateTime();
            OffsetDateTime end = today.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
            User user = preference.getUser();
            if (timeEntryRepository.existsByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                    user.getId(),
                    start,
                    end
            )) {
                continue;
            }
            notificationService.createReminder(new ReminderNotificationDTO(
                    WorkspaceType.PERSONAL,
                    user,
                    null,
                    user,
                    NotificationType.MISSING_DAILY_TIME,
                    ReminderNotificationText.missingDailyTimeMessage(today),
                    ReminderNotificationText.SUBJECT_TYPE_CALENDAR_DAY,
                    null,
                    today.toString(),
                    ReminderNotificationText.calendarDayRoute(today),
                    ReminderNotificationText.SOURCE_LABEL_OPEN_CALENDAR_DAY,
                    ReminderNotificationText.missingDailyTimeReminderKey(user.getId(), today),
                    now,
                    now.plusHours(emailEscalationHours)
            ));
        }
    }

    private void createInvoicePeriodReminders(OffsetDateTime now) {
        for (UserPreference preference : preferenceRepository.findAll()) {
            if (!preference.isInvoiceRemindersEnabled()) {
                continue;
            }
            ZoneId zone = zone(preference.getTimezone());
            LocalDate localToday = now.atZoneSameInstant(zone).toLocalDate();
            if (localToday.getDayOfMonth() != 1) {
                continue;
            }
            YearMonth previousMonth = YearMonth.from(localToday.minusMonths(1));
            LocalDate periodStart = previousMonth.atDay(1);
            LocalDate periodEnd = previousMonth.atEndOfMonth();
            User user = preference.getUser();
            if (!invoiceRepository.existsByUserIdAndPeriodStartAndPeriodEnd(user.getId(), periodStart, periodEnd)) {
                notificationService.createReminder(new ReminderNotificationDTO(
                        WorkspaceType.PERSONAL,
                        user,
                        null,
                        user,
                        NotificationType.INVOICE_PERIOD_REMINDER,
                        ReminderNotificationText.invoicePeriodMessage(previousMonth),
                        ReminderNotificationText.SUBJECT_TYPE_INVOICE_PERIOD,
                        null,
                        previousMonth.toString(),
                        ReminderNotificationText.SOURCE_ROUTE_INVOICE,
                        ReminderNotificationText.SOURCE_LABEL_OPEN_INVOICE,
                        ReminderNotificationText.invoicePersonalReminderKey(user.getId(), previousMonth),
                        now,
                        now.plusHours(emailEscalationHours)
                ));
            }
        }

        for (OrganizationMember member : organizationMemberRepository.findAll()) {
            if (member.getRole() != OrganizationRole.OWNER && member.getRole() != OrganizationRole.ADMIN) {
                continue;
            }
            UserPreference preference = preferenceRepository.findById(member.getUser().getId()).orElse(null);
            if (preference == null || !preference.isInvoiceRemindersEnabled()) {
                continue;
            }
            ZoneId zone = zone(preference.getTimezone());
            LocalDate localToday = now.atZoneSameInstant(zone).toLocalDate();
            if (localToday.getDayOfMonth() != 1) {
                continue;
            }
            YearMonth previousMonth = YearMonth.from(localToday.minusMonths(1));
            LocalDate periodStart = previousMonth.atDay(1);
            LocalDate periodEnd = previousMonth.atEndOfMonth();
            if (invoiceRepository.existsByOrganizationIdAndPeriodStartAndPeriodEnd(
                    member.getOrganization().getId(),
                    periodStart,
                    periodEnd
            )) {
                continue;
            }
            notificationService.createReminder(new ReminderNotificationDTO(
                    WorkspaceType.ORGANIZATION,
                    null,
                    member.getOrganization(),
                    member.getUser(),
                    NotificationType.INVOICE_PERIOD_REMINDER,
                    ReminderNotificationText.organizationInvoicePeriodMessage(member.getOrganization().getName(), previousMonth),
                    ReminderNotificationText.SUBJECT_TYPE_INVOICE_PERIOD,
                    member.getOrganization().getId(),
                    previousMonth.toString(),
                    ReminderNotificationText.SOURCE_ROUTE_INVOICE,
                    ReminderNotificationText.SOURCE_LABEL_OPEN_INVOICE,
                    ReminderNotificationText.invoiceOrganizationReminderKey(
                            member.getOrganization().getId(),
                            member.getUser().getId(),
                            previousMonth
                    ),
                    now,
                    now.plusHours(emailEscalationHours)
            ));
        }
    }

    private WorkspaceType workspaceType(TimeEntry entry) {
        return entry.getOrganization() == null ? WorkspaceType.PERSONAL : WorkspaceType.ORGANIZATION;
    }

    private User workspaceUser(TimeEntry entry) {
        return entry.getOrganization() == null ? entry.getUser() : null;
    }

    private String projectLabel(TimeEntry entry) {
        String task = entry.getTask() == null ? null : entry.getTask().getName();
        return task == null || task.isBlank() ? projectName(entry) : projectName(entry) + " / " + task;
    }

    private String projectName(TimeEntry entry) {
        return entry.getProject() == null ? entry.getProjectName() : entry.getProject().getName();
    }

    private ZoneId zone(String timezone) {
        try {
            return ZoneId.of(timezone == null || timezone.isBlank() ? "UTC" : timezone);
        } catch (RuntimeException exception) {
            return ZoneOffset.UTC;
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
