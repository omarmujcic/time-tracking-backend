package com.omarmujcic.timetracking.core.settings.entity;

import java.time.OffsetDateTime;

import com.omarmujcic.timetracking.core.auth.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private java.util.UUID userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 12)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme_mode", nullable = false, length = 20)
    private ThemeMode themeMode;

    @Column(name = "grouped_entries_enabled", nullable = false)
    private boolean groupedEntriesEnabled;

    @Column(name = "include_organization_entries_in_personal_reports", nullable = false)
    private boolean includeOrganizationEntriesInPersonalReports;

    @Column(name = "long_timer_reminders_enabled", nullable = false)
    private boolean longTimerRemindersEnabled = true;

    @Column(name = "missing_daily_time_reminders_enabled", nullable = false)
    private boolean missingDailyTimeRemindersEnabled = true;

    @Column(name = "invoice_reminders_enabled", nullable = false)
    private boolean invoiceRemindersEnabled = true;

    @Column(name = "browser_push_enabled", nullable = false)
    private boolean browserPushEnabled;

    @Column(name = "date_format", nullable = false, length = 32)
    private String dateFormat;

    @Enumerated(EnumType.STRING)
    @Column(name = "decimal_separator", nullable = false, length = 8)
    private DecimalSeparator decimalSeparator;

    @Column(nullable = false, length = 80)
    private String timezone;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
