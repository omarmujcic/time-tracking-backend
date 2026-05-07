package com.omarmujcic.timetracking.core.timetracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByUserIdOrderByStartedAtDesc(UUID userId);

    Optional<TimeEntry> findByIdAndUserId(UUID id, UUID userId);

    Optional<TimeEntry> findByUserIdAndEndedAtIsNull(UUID userId);
}
