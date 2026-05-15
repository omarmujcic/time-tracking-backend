package com.omarmujcic.timetracking.core.billing;

import java.math.BigDecimal;
import java.util.UUID;

import com.omarmujcic.timetracking.core.projects.entity.Project;

public record BillingLine(
        String projectKey,
        UUID projectId,
        Project project,
        String projectName,
        String description,
        long durationSeconds,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String currency
) {
}
