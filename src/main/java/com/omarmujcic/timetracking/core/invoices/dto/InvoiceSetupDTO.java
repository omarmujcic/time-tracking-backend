package com.omarmujcic.timetracking.core.invoices.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

public record InvoiceSetupDTO(
        WorkspaceType workspaceType,
        UUID organizationId,
        String workspaceName,
        InvoicePartyDTO from,
        InvoicePartyDTO to,
        int nextInvoiceNumber,
        String suggestedInvoiceNumber,
        String taxLabel,
        BigDecimal taxRate,
        String terms,
        int dueDays,
        String currency,
        int workspaceNextInvoiceNumber,
        String workspaceSuggestedInvoiceNumber,
        String workspaceTaxLabel,
        BigDecimal workspaceTaxRate,
        String workspaceTerms,
        int workspaceDueDays,
        boolean ready,
        boolean fromReady,
        boolean toReady,
        boolean canManageUserSettings,
        boolean canManageWorkspaceSettings,
        boolean canManageSetup
) {
}
