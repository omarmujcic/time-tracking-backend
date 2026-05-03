package com.omarmujcic.timetracking.core.auth;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.config.security.JwtService;
import com.omarmujcic.timetracking.core.auth.dto.AuthRequestDTO;
import com.omarmujcic.timetracking.core.auth.dto.AuthResponseDTO;
import com.omarmujcic.timetracking.core.auth.dto.RegisterRequestDTO;
import com.omarmujcic.timetracking.core.auth.dto.UserResponseDTO;
import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.auth.mapper.UserMapper;
import com.omarmujcic.timetracking.core.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String username = normalizeUsername(request.getUsername());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = userMapper.toEntity(request, username, passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);
        return authResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(AuthRequestDTO request) {
        User user = userRepository.findByUsernameIgnoreCase(normalizeUsername(request.getUsername()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return authResponse(user);
    }

    public UserResponseDTO toResponse(User user) {
        return userMapper.toResponseDTO(user);
    }

    private AuthResponseDTO authResponse(User user) {
        return userMapper.toAuthResponseDTO(jwtService.createToken(user.getUsername()), toResponse(user));
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
