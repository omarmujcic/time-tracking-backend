package com.omarmujcic.timetracking.core.reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

final class TaskSegmentTotals {

    private final UUID taskId;
    private final String taskName;
    private final String projectName;
    private final String label;
    private long seconds;
    private BigDecimal amount = BigDecimal.ZERO.setScale(2);

    TaskSegmentTotals(TaskSegmentKey key) {
        this.taskId = key.taskId();
        this.taskName = key.taskName();
        this.projectName = key.projectName();
        this.label = key.label();
    }

    void add(long seconds, BigDecimal amount) {
        this.seconds += seconds;
        this.amount = this.amount.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    UUID taskId() {
        return taskId;
    }

    String taskName() {
        return taskName;
    }

    String projectName() {
        return projectName;
    }

    long seconds() {
        return seconds;
    }

    BigDecimal amount() {
        return amount;
    }

    String label() {
        return label == null ? "" : label;
    }
}
