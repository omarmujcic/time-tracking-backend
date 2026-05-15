package com.omarmujcic.timetracking.core.billing;

import java.math.BigDecimal;
import java.util.List;

public record BillingReportResult(
        BigDecimal totalAmount,
        List<BillingProjectTotal> projectTotals
) {
}
