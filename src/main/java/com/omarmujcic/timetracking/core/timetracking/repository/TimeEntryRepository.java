package com.omarmujcic.timetracking.core.timetracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByUserIdOrderByStartedAtDesc(UUID userId);

    List<TimeEntry> findByOrganizationIdOrderByStartedAtDesc(UUID organizationId);

    @Query("select entry from TimeEntry entry join fetch entry.user order by entry.startedAt desc")
    List<TimeEntry> findAllWithUserOrderByStartedAtDesc();

    Optional<TimeEntry> findByIdAndUserId(UUID id, UUID userId);

    Optional<TimeEntry> findByUserIdAndEndedAtIsNull(UUID userId);

    boolean existsByProjectId(UUID projectId);

    boolean existsByTaskId(UUID taskId);
}
