package com.omarmujcic.timetracking.core.settings.dto;

import com.omarmujcic.timetracking.core.auth.dto.AuthResponseDTO;

public record UpdateAccountProfileResponseDTO(AccountProfileDTO profile, AuthResponseDTO session) {
}
