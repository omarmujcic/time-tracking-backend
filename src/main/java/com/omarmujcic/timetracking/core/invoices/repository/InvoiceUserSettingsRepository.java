package com.omarmujcic.timetracking.core.invoices.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.invoices.entity.InvoiceUserSettings;

public interface InvoiceUserSettingsRepository extends JpaRepository<InvoiceUserSettings, UUID> {

    Optional<InvoiceUserSettings> findByUserIdAndWorkspaceTypeAndOrganizationIsNull(
            UUID userId,
            com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType workspaceType
    );

    Optional<InvoiceUserSettings> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
