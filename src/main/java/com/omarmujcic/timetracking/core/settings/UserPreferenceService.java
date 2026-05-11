package com.omarmujcic.timetracking.core.settings;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.settings.dto.UpdateUserPreferenceRequestDTO;
import com.omarmujcic.timetracking.core.settings.dto.UserPreferenceDTO;
import com.omarmujcic.timetracking.core.settings.entity.UserPreference;
import com.omarmujcic.timetracking.core.settings.mapper.UserPreferenceMapper;
import com.omarmujcic.timetracking.core.settings.repository.UserPreferenceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserPreferenceMapper userPreferenceMapper;

    @Transactional(readOnly = true)
    public UserPreferenceDTO get(User user) {
        return userPreferenceMapper.toDTO(findOrCreate(user));
    }

    @Transactional
    public UserPreferenceDTO update(User user, UpdateUserPreferenceRequestDTO request) {
        UserPreference preference = findOrCreate(user);
        userPreferenceMapper.updatePreference(request, now(), preference);
        return userPreferenceMapper.toDTO(preference);
    }

    private UserPreference findOrCreate(User user) {
        return userPreferenceRepository.findById(user.getId()).orElseGet(() -> {
            UserPreference preference = userPreferenceMapper.defaultPreference(user, TimeZone.getDefault().getID(), now());
            return userPreferenceRepository.save(preference);
        });
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
