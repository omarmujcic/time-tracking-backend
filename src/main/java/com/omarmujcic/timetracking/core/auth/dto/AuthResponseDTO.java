package com.omarmujcic.timetracking.core.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDTO {

    private String token;

    private UserResponseDTO user;
}
