package com.omarmujcic.timetracking.core.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordRequestDTO {
    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 120)
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
