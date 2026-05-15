package com.omarmujcic.timetracking.core.billing;

import java.math.BigDecimal;

public record BillingProjectTotal(
        String projectName,
        long totalSeconds,
        BigDecimal totalAmount
) {
}
