package com.omarmujcic.timetracking.core.settings;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.config.security.JwtService;
import com.omarmujcic.timetracking.core.auth.dto.AuthResponseDTO;
import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.auth.mapper.UserMapper;
import com.omarmujcic.timetracking.core.auth.repository.UserRepository;
import com.omarmujcic.timetracking.core.settings.dto.AccountProfileDTO;
import com.omarmujcic.timetracking.core.settings.dto.UpdateAccountProfileRequestDTO;
import com.omarmujcic.timetracking.core.settings.dto.UpdateAccountProfileResponseDTO;
import com.omarmujcic.timetracking.core.settings.dto.UpdatePasswordRequestDTO;
import com.omarmujcic.timetracking.core.settings.mapper.AccountMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public AccountProfileDTO profile(User user) {
        return accountMapper.toProfileDTO(user);
    }

    @Transactional
    public UpdateAccountProfileResponseDTO updateProfile(User user, UpdateAccountProfileRequestDTO request) {
        User managed = userRepository.findById(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        String username = normalizeUsername(request.getUsername());
        String email = normalizeNullable(request.getEmail());
        String phone = normalizeNullable(request.getPhone());

        userRepository.findByUsernameIgnoreCase(username)
            .filter(existing -> !existing.getId().equals(user.getId()))
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            });
        if (email != null) {
            userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                });
        }

        accountMapper.updateProfile(username, request.getDisplayName().trim(), email, phone, managed);

        AuthResponseDTO session = userMapper.toAuthResponseDTO(jwtService.createToken(managed.getId().toString()),
                userMapper.toResponseDTO(managed));
        return new UpdateAccountProfileResponseDTO(accountMapper.toProfileDTO(managed), session);
    }

    @Transactional
    public void updatePassword(User user, UpdatePasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password confirmation does not match");
        }
        User managed = userRepository.findById(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), managed.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        managed.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
