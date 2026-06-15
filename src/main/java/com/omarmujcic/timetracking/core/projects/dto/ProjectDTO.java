package com.omarmujcic.timetracking.core.projects.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.omarmujcic.timetracking.core.projects.entity.ProjectStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDTO {
    private UUID id;
    private String name;
    private String ticketPrefix;
    private ProjectStatus status;
    private BigDecimal hourlyRate;
    private String currency;
    private ProjectBillingRuleDTO billingRule;
    private List<TaskDTO> tasks;
}
