package com.omarmujcic.timetracking.core.projects.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.projects.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByUserIdOrderByNameAsc(UUID userId);

    List<Project> findByOrganizationIdOrderByNameAsc(UUID organizationId);

    Optional<Project> findByIdAndUserId(UUID id, UUID userId);

    Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);

}
