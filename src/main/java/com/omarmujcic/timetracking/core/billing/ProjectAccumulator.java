package com.omarmujcic.timetracking.core.billing;

import static com.omarmujcic.timetracking.core.billing.BillingConstants.ZERO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import com.omarmujcic.timetracking.core.projects.entity.Project;

final class ProjectAccumulator {

    private final String projectKey;
    private final Project project;
    private final String projectName;
    private long seconds;
    private BigDecimal amount = ZERO;

    ProjectAccumulator(String projectKey, Project project, String projectName) {
        this.projectKey = projectKey;
        this.project = project;
        this.projectName = projectName;
    }

    void add(long seconds, BigDecimal amount) {
        this.seconds += seconds;
        this.amount = this.amount.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    String projectKey() {
        return projectKey;
    }

    UUID projectId() {
        return project == null ? null : project.getId();
    }

    Project project() {
        return project;
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
}
