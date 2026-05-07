package com.omarmujcic.timetracking.core.reports.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReportFilterOptionsDTO(
        List<ReportUserOptionDTO> users,
        List<String> projects,
        List<BigDecimal> rates
) {
    public record ReportUserOptionDTO(UUID id, String username, String displayName) {
    }
}
