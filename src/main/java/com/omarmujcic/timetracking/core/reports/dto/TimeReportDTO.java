package com.omarmujcic.timetracking.core.reports.dto;

import java.util.List;

public record TimeReportDTO(
        ReportSummaryDTO summary,
        List<ReportBucketDTO> buckets,
        List<ReportProjectDTO> projects,
        List<ReportEntryDTO> entries
) {
}
