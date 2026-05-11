package com.omarmujcic.timetracking.core.settings.dto;

import com.omarmujcic.timetracking.core.settings.entity.DecimalSeparator;
import com.omarmujcic.timetracking.core.settings.entity.ThemeMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserPreferenceRequestDTO {
    @NotBlank
    @Size(max = 12)
    private String language;

    @NotNull
    private ThemeMode themeMode;

    private boolean groupedEntriesEnabled;

    @NotBlank
    @Size(max = 32)
    private String dateFormat;

    @NotNull
    private DecimalSeparator decimalSeparator;

    @NotBlank
    @Size(max = 80)
    private String timezone;
}
