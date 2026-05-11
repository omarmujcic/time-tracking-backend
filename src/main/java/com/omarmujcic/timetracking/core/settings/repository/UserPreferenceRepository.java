package com.omarmujcic.timetracking.core.settings.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.settings.entity.UserPreference;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {
}
