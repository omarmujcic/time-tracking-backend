package com.omarmujcic.timetracking.core.settings;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.settings.dto.AccountProfileDTO;
import com.omarmujcic.timetracking.core.settings.dto.UpdateAccountProfileRequestDTO;
import com.omarmujcic.timetracking.core.settings.dto.UpdateAccountProfileResponseDTO;
import com.omarmujcic.timetracking.core.settings.dto.UpdatePasswordRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/profile")
    public AccountProfileDTO profile(@AuthenticationPrincipal User user) {
        return accountService.profile(user);
    }

    @PutMapping("/profile")
    public UpdateAccountProfileResponseDTO updateProfile(@AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateAccountProfileRequestDTO request) {
        return accountService.updateProfile(user, request);
    }

    @PutMapping("/password")
    public void updatePassword(@AuthenticationPrincipal User user, @Valid @RequestBody UpdatePasswordRequestDTO request) {
        accountService.updatePassword(user, request);
    }
}
