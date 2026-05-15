package com.omarmujcic.timetracking.core.projects.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.omarmujcic.timetracking.core.projects.entity.ProjectBillingRuleType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectBillingRuleDTO {
    private UUID id;
    private ProjectBillingRuleType type;
    private LocalDate effectiveFrom;
    private BigDecimal monthlyAmount;
    private BigDecimal baseAmount;
    private BigDecimal includedHours;
    private BigDecimal overageHourlyRate;
}
