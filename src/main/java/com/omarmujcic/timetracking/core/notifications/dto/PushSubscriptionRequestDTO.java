package com.omarmujcic.timetracking.core.notifications.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushSubscriptionRequestDTO {
    @NotBlank
    private String endpoint;

    @Valid
    @NotNull
    private PushSubscriptionKeysDTO keys;

    @Size(max = 500)
    private String userAgent;
}
