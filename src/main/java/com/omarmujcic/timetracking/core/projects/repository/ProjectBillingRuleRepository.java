package com.omarmujcic.timetracking.core.projects.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.projects.entity.ProjectBillingRule;

public interface ProjectBillingRuleRepository extends JpaRepository<ProjectBillingRule, UUID> {

    List<ProjectBillingRule> findByProjectIdOrderByEffectiveFromAsc(UUID projectId);

    List<ProjectBillingRule> findByProjectIdInOrderByProjectIdAscEffectiveFromAsc(Collection<UUID> projectIds);

    Optional<ProjectBillingRule> findFirstByProjectIdOrderByEffectiveFromDesc(UUID projectId);

    Optional<ProjectBillingRule> findByProjectIdAndEffectiveFrom(UUID projectId, LocalDate effectiveFrom);
}
