package com.omarmujcic.timetracking.core.invoices.repository;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omarmujcic.timetracking.core.invoices.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Invoice> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    List<Invoice> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Invoice> findTop20ByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    boolean existsByUserIdAndPeriodStartAndPeriodEnd(UUID userId, LocalDate periodStart, LocalDate periodEnd);

    boolean existsByOrganizationIdAndPeriodStartAndPeriodEnd(UUID organizationId, LocalDate periodStart,
            LocalDate periodEnd);
}
