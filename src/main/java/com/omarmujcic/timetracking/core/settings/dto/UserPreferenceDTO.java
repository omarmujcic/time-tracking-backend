package com.omarmujcic.timetracking.core.settings.dto;

import com.omarmujcic.timetracking.core.settings.entity.DecimalSeparator;
import com.omarmujcic.timetracking.core.settings.entity.ThemeMode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPreferenceDTO {
    private String language;
    private ThemeMode themeMode;
    private boolean groupedEntriesEnabled;
    private boolean includeOrganizationEntriesInPersonalReports;
    private String dateFormat;
    private DecimalSeparator decimalSeparator;
    private String timezone;
}
