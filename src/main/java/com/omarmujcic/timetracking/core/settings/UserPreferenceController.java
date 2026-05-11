package com.omarmujcic.timetracking.core.settings;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.settings.dto.UpdateUserPreferenceRequestDTO;
import com.omarmujcic.timetracking.core.settings.dto.UserPreferenceDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/settings/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public UserPreferenceDTO get(@AuthenticationPrincipal User user) {
        return userPreferenceService.get(user);
    }

    @PutMapping
    public UserPreferenceDTO update(@AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserPreferenceRequestDTO request) {
        return userPreferenceService.update(user, request);
    }
}
