package com.omarmujcic.timetracking.core.settings.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountProfileRequestDTO {
    @NotBlank
    @Size(max = 120)
    private String username;

    @NotBlank
    @Size(max = 160)
    private String displayName;

    @Email
    @Size(max = 254)
    private String email;

    @Pattern(regexp = "^[+()0-9 .-]{0,40}$")
    private String phone;
}
