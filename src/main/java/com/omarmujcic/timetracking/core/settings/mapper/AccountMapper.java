package com.omarmujcic.timetracking.core.settings.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.settings.dto.AccountProfileDTO;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountProfileDTO toProfileDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "activeWorkspaceType", ignore = true)
    @Mapping(target = "activeOrganization", ignore = true)
    void updateProfile(String username, String displayName, String email, String phone, @MappingTarget User user);
}
