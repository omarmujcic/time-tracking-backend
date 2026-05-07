package com.omarmujcic.timetracking.core.timetracking;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.timetracking.dto.CreateTimeEntryRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.StartTimerRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.TimeEntryResponseDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.TimeEntrySummaryDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.UpdateTimeEntryRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;
import com.omarmujcic.timetracking.core.timetracking.mapper.TimeEntryMapper;
import com.omarmujcic.timetracking.core.timetracking.repository.TimeEntryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryMapper timeEntryMapper;

    @Transactional(readOnly = true)
    public List<TimeEntryResponseDTO> list(User user, YearMonth month, LocalDate day, String project, UUID userId) {
        OffsetDateTime now = now();
        return timeEntryMapper.toResponseDTOs(filteredEntries(user, month, day, project, userId, now), now);
    }

    @Transactional(readOnly = true)
    public TimeEntrySummaryDTO summary(User user, YearMonth month, LocalDate day, String project, UUID userId) {
        OffsetDateTime now = now();
        return timeEntryMapper.toSummaryDTO(filteredEntries(user, month, day, project, userId, now), now);
    }

    @Transactional
    public TimeEntryResponseDTO start(User user, StartTimerRequestDTO request) {
        OffsetDateTime now = now();
        if (timeEntryRepository.findByUserIdAndEndedAtIsNull(user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active timer is already running");
        }

        TimeEntry entry = timeEntryMapper.toTimerEntity(request, user, now);
        assertNoOverlap(user, entry.getStartedAt(), null, null, now);
        return timeEntryMapper.toResponseDTO(timeEntryRepository.save(entry), now);
    }

    @Transactional
    public TimeEntryResponseDTO stop(User user, UUID id) {
        OffsetDateTime now = now();
        TimeEntry entry = findOwnedEntry(user, id);
        if (entry.getEndedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Timer is already stopped");
        }
        if (!entry.getStartedAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timer cannot end before it starts");
        }

        timeEntryMapper.stopTimer(now, entry);
        assertNoOverlap(user, entry.getStartedAt(), entry.getEndedAt(), entry.getId(), now);
        return timeEntryMapper.toResponseDTO(entry, now);
    }

    @Transactional
    public TimeEntryResponseDTO create(User user, CreateTimeEntryRequestDTO request) {
        OffsetDateTime now = now();
        OffsetDateTime startedAt = timeEntryMapper.truncateToSeconds(request.getStartedAt());
        OffsetDateTime endedAt = timeEntryMapper.truncateToSeconds(request.getEndedAt());
        assertValidCompletedRange(startedAt, endedAt);
        assertNoOverlap(user, startedAt, endedAt, null, now);

        TimeEntry entry = timeEntryMapper.toManualEntity(request, user, startedAt, endedAt, now);
        return timeEntryMapper.toResponseDTO(timeEntryRepository.save(entry), now);
    }

    @Transactional
    public TimeEntryResponseDTO update(User user, UUID id, UpdateTimeEntryRequestDTO request) {
        OffsetDateTime now = now();
        TimeEntry entry = findOwnedEntry(user, id);
        OffsetDateTime startedAt = timeEntryMapper.truncateToSeconds(request.getStartedAt());
        OffsetDateTime endedAt = timeEntryMapper.truncateToSeconds(request.getEndedAt());

        if (endedAt != null) {
            assertValidCompletedRange(startedAt, endedAt);
        } else if (timeEntryRepository.findByUserIdAndEndedAtIsNull(user.getId())
                .filter(activeEntry -> !activeEntry.getId().equals(id))
                .isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active timer is already running");
        }

        assertNoOverlap(user, startedAt, endedAt, id, now);
        timeEntryMapper.updateEntity(request, startedAt, endedAt, now, entry);
        return timeEntryMapper.toResponseDTO(entry, now);
    }

    @Transactional
    public void delete(User user, UUID id) {
        timeEntryRepository.delete(findOwnedEntry(user, id));
    }

    private TimeEntry findOwnedEntry(User user, UUID id) {
        return timeEntryRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Time entry not found"));
    }

    private List<TimeEntry> filteredEntries(User user, YearMonth month, LocalDate day, String project, UUID userId,
            OffsetDateTime now) {
        if (userId != null && !user.getId().equals(userId)) {
            return List.of();
        }

        String normalizedProject = project == null ? null : project.trim().toLowerCase();
        return timeEntryRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
            .filter(entry -> isWithinDateFilters(entry, month, day, now))
            .filter(entry -> normalizedProject == null || normalizedProject.isBlank()
                || entry.getProjectName().toLowerCase().contains(normalizedProject))
            .toList();
    }

    private boolean isWithinDateFilters(TimeEntry entry, YearMonth month, LocalDate day, OffsetDateTime now) {
        LocalDate entryDay = entry.getStartedAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
        if (day != null) {
            return entryDay.equals(day);
        }
        if (month != null) {
            return YearMonth.from(entryDay).equals(month);
        }
        return true;
    }

    private void assertValidCompletedRange(OffsetDateTime startedAt, OffsetDateTime endedAt) {
        if (!startedAt.isBefore(endedAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
    }

    private void assertNoOverlap(User user, OffsetDateTime startedAt, OffsetDateTime endedAt, UUID ignoredId,
            OffsetDateTime now) {
        boolean hasOverlap = timeEntryRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
            .filter(entry -> ignoredId == null || !entry.getId().equals(ignoredId))
            .anyMatch(entry -> overlaps(startedAt, endedAt, entry.getStartedAt(), entry.getEndedAt(), now));

        if (hasOverlap) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Time entry overlaps an existing entry");
        }
    }

    private boolean overlaps(OffsetDateTime startedAt, OffsetDateTime endedAt, OffsetDateTime existingStartedAt,
            OffsetDateTime existingEndedAt, OffsetDateTime now) {
        OffsetDateTime newEnd = endedAt == null ? OffsetDateTime.MAX : endedAt;
        OffsetDateTime existingEnd = existingEndedAt == null ? OffsetDateTime.MAX : existingEndedAt;
        return startedAt.isBefore(existingEnd) && existingStartedAt.isBefore(newEnd);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    }
}
