package com.omarmujcic.timetracking.core.auth.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {

    private UUID id;

    private String username;

    private String displayName;
}
