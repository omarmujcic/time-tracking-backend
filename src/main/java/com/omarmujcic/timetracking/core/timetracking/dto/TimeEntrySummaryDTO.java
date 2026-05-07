package com.omarmujcic.timetracking.core.timetracking.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntrySummaryDTO {

    private long totalSeconds;
    private BigDecimal totalAmount;
    private String currency;
    private int entryCount;
    private boolean hasActiveTimer;
}
