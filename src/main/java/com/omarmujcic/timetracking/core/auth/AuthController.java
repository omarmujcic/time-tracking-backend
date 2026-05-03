package com.omarmujcic.timetracking.core.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.dto.AuthRequestDTO;
import com.omarmujcic.timetracking.core.auth.dto.AuthResponseDTO;
import com.omarmujcic.timetracking.core.auth.dto.RegisterRequestDTO;
import com.omarmujcic.timetracking.core.auth.dto.UserResponseDTO;
import com.omarmujcic.timetracking.core.auth.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody AuthRequestDTO request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponseDTO me(@AuthenticationPrincipal User user) {
        return authService.toResponse(user);
    }
}
