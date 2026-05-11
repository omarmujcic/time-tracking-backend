package com.omarmujcic.timetracking.core.projects.dto;

import java.math.BigDecimal;

import com.omarmujcic.timetracking.core.projects.entity.ProjectStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpsertProjectRequestDTO {
    @NotBlank
    @Size(max = 160)
    private String name;

    @NotNull
    private ProjectStatus status;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal hourlyRate;
}
