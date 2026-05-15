package com.omarmujcic.timetracking.core.notifications.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectBillingIssueRequestDTO {

    @NotNull
    private UUID projectId;

    @NotBlank
    @Size(max = 2000)
    private String message;
}
