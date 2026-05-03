package com.omarmujcic.timetracking.core.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {

    @NotBlank
    @Size(max = 120)
    private String username;

    @NotBlank
    @Size(max = 160)
    private String displayName;

    @NotBlank
    @Size(min = 8, max = 120)
    private String password;
}
