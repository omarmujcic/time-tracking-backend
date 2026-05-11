package com.omarmujcic.timetracking.core.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationRequestDTO {
    @NotBlank
    @Size(max = 160)
    private String name;
}
