package com.omarmujcic.timetracking.core.settings.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountProfileDTO {
    private UUID id;
    private String username;
    private String displayName;
    private String email;
    private String phone;
}
