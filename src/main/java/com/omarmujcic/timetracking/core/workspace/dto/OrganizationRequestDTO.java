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

    @Size(max = 160)
    private String legalName;

    @Size(max = 220)
    private String businessAddressLine1;

    @Size(max = 220)
    private String businessAddressLine2;

    @Size(max = 40)
    private String businessPostalCode;

    @Size(max = 120)
    private String businessCity;

    @Size(max = 120)
    private String businessCountry;

    @Size(max = 80)
    private String timezone;

    @Size(max = 3)
    private String defaultCurrency;

    private Boolean membersCanCreateTasks;
}
