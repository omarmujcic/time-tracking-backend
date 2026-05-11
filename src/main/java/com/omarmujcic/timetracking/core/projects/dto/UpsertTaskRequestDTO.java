package com.omarmujcic.timetracking.core.projects.dto;

import com.omarmujcic.timetracking.core.projects.entity.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpsertTaskRequestDTO {
    @NotBlank
    @Size(max = 160)
    private String name;

    @NotNull
    private TaskStatus status;
}
