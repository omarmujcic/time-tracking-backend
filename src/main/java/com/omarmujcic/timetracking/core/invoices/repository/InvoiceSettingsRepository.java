package com.omarmujcic.timetracking.core.invoices.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.invoices.entity.InvoiceSettings;

public interface InvoiceSettingsRepository extends JpaRepository<InvoiceSettings, UUID> {

    Optional<InvoiceSettings> findByUserId(UUID userId);

    Optional<InvoiceSettings> findByOrganizationId(UUID organizationId);
}
