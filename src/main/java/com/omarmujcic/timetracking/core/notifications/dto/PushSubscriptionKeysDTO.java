package com.omarmujcic.timetracking.core.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushSubscriptionKeysDTO {
    @NotBlank
    private String p256dh;

    @NotBlank
    private String auth;
}
