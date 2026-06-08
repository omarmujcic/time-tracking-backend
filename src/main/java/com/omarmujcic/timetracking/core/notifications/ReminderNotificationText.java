package com.omarmujcic.timetracking.core.notifications;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

public final class ReminderNotificationText {

    public static final String SUBJECT_TYPE_CALENDAR_DAY = "CALENDAR_DAY";
    public static final String SUBJECT_TYPE_INVOICE_PERIOD = "INVOICE_PERIOD";
    public static final String SUBJECT_TYPE_TIME_ENTRY = "TIME_ENTRY";
    public static final String SOURCE_LABEL_OPEN_CALENDAR_DAY = "Open calendar day";
    public static final String SOURCE_LABEL_OPEN_INVOICE = "Open invoice";
    public static final String SOURCE_LABEL_OPEN_TIMER = "Open timer";
    public static final String SOURCE_ROUTE_INVOICE = "/time-trackz/invoice";
    public static final String SOURCE_ROUTE_TIMER = "/time-trackz/dashboard?editTimer=true";

    private static final String KEY_PREFIX_INVOICE_ORGANIZATION = "invoice-org:";
    private static final String KEY_PREFIX_INVOICE_PERSONAL = "invoice-personal:";
    private static final String KEY_PREFIX_LONG_TIMER = "long-timer:";
    private static final String KEY_PREFIX_MISSING_DAY = "missing-day:";

    private ReminderNotificationText() {
    }

    public static String calendarDayRoute(LocalDate day) {
        return "/time-trackz/calendar?day=" + day;
    }

    public static String invoiceOrganizationReminderKey(UUID organizationId, UUID userId, YearMonth previousMonth) {
        return KEY_PREFIX_INVOICE_ORGANIZATION + organizationId + ":" + userId + ":" + previousMonth;
    }

    public static String invoicePersonalReminderKey(UUID userId, YearMonth previousMonth) {
        return KEY_PREFIX_INVOICE_PERSONAL + userId + ":" + previousMonth;
    }

    public static String invoicePeriodMessage(YearMonth previousMonth) {
        return "The " + previousMonth + " invoice period is ready to review.";
    }

    public static String longTimerMessage(String project, long longTimerHours) {
        return "Your timer for " + project + " has been running for more than " + longTimerHours + " hours.";
    }

    public static String longTimerReminderKey(UUID timeEntryId) {
        return KEY_PREFIX_LONG_TIMER + timeEntryId;
    }

    public static String missingDailyTimeMessage(LocalDate day) {
        return "No time has been tracked for " + day + ". Add an entry if work was missed.";
    }

    public static String missingDailyTimeReminderKey(UUID userId, LocalDate day) {
        return KEY_PREFIX_MISSING_DAY + userId + ":" + day;
    }

    public static String organizationInvoicePeriodMessage(String organizationName, YearMonth previousMonth) {
        return organizationName + "'s " + previousMonth + " invoice period is ready to review.";
    }
}
