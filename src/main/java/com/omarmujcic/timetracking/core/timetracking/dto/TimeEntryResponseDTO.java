package com.omarmujcic.timetracking.core.timetracking.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeEntryResponseDTO {

    private UUID id;
    private UUID userId;
    private String username;
    private String displayName;
    private String projectName;
    private String description;
    private BigDecimal hourlyRate;
    private String currency;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private long durationSeconds;
    private BigDecimal billableAmount;
    private boolean active;
}
