package com.omarmujcic.timetracking.core.projects.dto;

import java.util.UUID;

import com.omarmujcic.timetracking.core.projects.entity.TaskStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDTO {
    private UUID id;
    private UUID projectId;
    private String name;
    private TaskStatus status;
}
