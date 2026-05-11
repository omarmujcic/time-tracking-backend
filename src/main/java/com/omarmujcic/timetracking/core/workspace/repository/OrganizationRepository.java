package com.omarmujcic.timetracking.core.workspace.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.workspace.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByJoinCodeIgnoreCase(String joinCode);

    boolean existsByJoinCodeIgnoreCase(String joinCode);
}
