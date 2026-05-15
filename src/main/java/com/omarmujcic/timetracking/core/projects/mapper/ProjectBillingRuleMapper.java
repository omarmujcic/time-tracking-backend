package com.omarmujcic.timetracking.core.projects.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.omarmujcic.timetracking.core.projects.dto.ProjectBillingRuleDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertProjectBillingRuleRequestDTO;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.projects.entity.ProjectBillingRule;
import com.omarmujcic.timetracking.core.projects.entity.ProjectBillingRuleType;

@Mapper(componentModel = "spring", imports = UUID.class)
public interface ProjectBillingRuleMapper {

    ProjectBillingRuleDTO toDTO(ProjectBillingRule rule);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "effectiveFrom", expression = "java(normalizeEffectiveFrom(request.getEffectiveFrom()))")
    @Mapping(target = "monthlyAmount", expression = "java(monthlyAmount(request))")
    @Mapping(target = "baseAmount", expression = "java(baseAmount(request))")
    @Mapping(target = "includedHours", expression = "java(includedHours(request))")
    @Mapping(target = "overageHourlyRate", expression = "java(overageHourlyRate(request))")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    ProjectBillingRule toEntity(UpsertProjectBillingRuleRequestDTO request, Project project, OffsetDateTime now);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "effectiveFrom", expression = "java(normalizeEffectiveFrom(request.getEffectiveFrom()))")
    @Mapping(target = "monthlyAmount", expression = "java(monthlyAmount(request))")
    @Mapping(target = "baseAmount", expression = "java(baseAmount(request))")
    @Mapping(target = "includedHours", expression = "java(includedHours(request))")
    @Mapping(target = "overageHourlyRate", expression = "java(overageHourlyRate(request))")
    @Mapping(target = "updatedAt", source = "now")
    void updateEntity(UpsertProjectBillingRuleRequestDTO request, OffsetDateTime now,
            @MappingTarget ProjectBillingRule rule);

    default LocalDate normalizeEffectiveFrom(LocalDate value) {
        return value == null ? null : value.withDayOfMonth(1);
    }

    default BigDecimal monthlyAmount(UpsertProjectBillingRuleRequestDTO request) {
        return request.getType() == ProjectBillingRuleType.FIXED_MONTHLY
                ? money(request.getMonthlyAmount())
                : null;
    }

    default BigDecimal baseAmount(UpsertProjectBillingRuleRequestDTO request) {
        return request.getType() == ProjectBillingRuleType.MONTHLY_BASE_PLUS_OVERAGE
                ? money(request.getBaseAmount())
                : null;
    }

    default BigDecimal includedHours(UpsertProjectBillingRuleRequestDTO request) {
        return request.getType() == ProjectBillingRuleType.MONTHLY_BASE_PLUS_OVERAGE
                ? money(request.getIncludedHours())
                : null;
    }

    default BigDecimal overageHourlyRate(UpsertProjectBillingRuleRequestDTO request) {
        return request.getType() == ProjectBillingRuleType.MONTHLY_BASE_PLUS_OVERAGE
                ? money(request.getOverageHourlyRate())
                : null;
    }

    default BigDecimal money(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }
}
