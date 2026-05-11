package com.omarmujcic.timetracking.core.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinOrganizationRequestDTO {
    @NotBlank
    @Size(max = 24)
    private String joinCode;
}
