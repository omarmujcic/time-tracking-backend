package com.omarmujcic.timetracking.core.timetracking.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTimeEntryRequestDTO {

    @NotBlank
    @Size(max = 160)
    private String projectName;

    @Size(max = 500)
    private String description;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal hourlyRate;

    @NotNull
    private OffsetDateTime startedAt;

    @NotNull
    private OffsetDateTime endedAt;
}
