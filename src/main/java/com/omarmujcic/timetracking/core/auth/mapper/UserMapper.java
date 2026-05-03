package com.omarmujcic.timetracking.core.auth.mapper;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.omarmujcic.timetracking.core.auth.dto.AuthResponseDTO;
import com.omarmujcic.timetracking.core.auth.dto.RegisterRequestDTO;
import com.omarmujcic.timetracking.core.auth.dto.UserResponseDTO;
import com.omarmujcic.timetracking.core.auth.entity.User;

@Mapper(componentModel = "spring", imports = {OffsetDateTime.class, UUID.class})
public interface UserMapper {

    UserResponseDTO toResponseDTO(User user);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "displayName", source = "request.displayName")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "createdAt", expression = "java(OffsetDateTime.now())")
    User toEntity(RegisterRequestDTO request, String username, String passwordHash);

    @Mapping(target = "token", source = "token")
    @Mapping(target = "user", source = "user")
    AuthResponseDTO toAuthResponseDTO(String token, UserResponseDTO user);
}
