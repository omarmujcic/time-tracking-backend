package com.omarmujcic.timetracking.core.projects.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.omarmujcic.timetracking.core.projects.entity.ProjectBillingRuleType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpsertProjectBillingRuleRequestDTO {
    @NotNull
    private ProjectBillingRuleType type;

    @NotNull
    private LocalDate effectiveFrom;

    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal monthlyAmount;

    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal baseAmount;

    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal includedHours;

    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal overageHourlyRate;
}
