package com.omarmujcic.timetracking.core.settings.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.settings.dto.UpdateUserPreferenceRequestDTO;
import com.omarmujcic.timetracking.core.settings.dto.UserPreferenceDTO;
import com.omarmujcic.timetracking.core.settings.entity.UserPreference;

@Mapper(componentModel = "spring")
public interface UserPreferenceMapper {
    UserPreferenceDTO toDTO(UserPreference preference);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "language", constant = "en")
    @Mapping(target = "themeMode", constant = "SYSTEM")
    @Mapping(target = "groupedEntriesEnabled", constant = "true")
    @Mapping(target = "includeOrganizationEntriesInPersonalReports", constant = "true")
    @Mapping(target = "longTimerRemindersEnabled", constant = "true")
    @Mapping(target = "missingDailyTimeRemindersEnabled", constant = "true")
    @Mapping(target = "invoiceRemindersEnabled", constant = "true")
    @Mapping(target = "browserPushEnabled", constant = "false")
    @Mapping(target = "dateFormat", constant = "YYYY-MM-DD")
    @Mapping(target = "decimalSeparator", constant = "DOT")
    @Mapping(target = "timezone", source = "timezone")
    @Mapping(target = "updatedAt", source = "updatedAt")
    UserPreference defaultPreference(User user, String timezone, java.time.OffsetDateTime updatedAt);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "language", expression = "java(request.getLanguage().trim())")
    @Mapping(target = "browserPushEnabled", ignore = true)
    @Mapping(target = "dateFormat", expression = "java(request.getDateFormat().trim())")
    @Mapping(target = "timezone", expression = "java(request.getTimezone().trim())")
    @Mapping(target = "updatedAt", source = "updatedAt")
    void updatePreference(UpdateUserPreferenceRequestDTO request, java.time.OffsetDateTime updatedAt,
            @MappingTarget UserPreference preference);
}
