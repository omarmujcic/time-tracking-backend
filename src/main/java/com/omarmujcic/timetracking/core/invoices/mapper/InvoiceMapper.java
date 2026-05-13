package com.omarmujcic.timetracking.core.invoices.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceHistoryItemDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceLineDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoicePartyDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceSetupDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceUserSettingsRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceWorkspaceSettingsRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceWorkLineDTO;
import com.omarmujcic.timetracking.core.invoices.entity.Invoice;
import com.omarmujcic.timetracking.core.invoices.entity.InvoiceLine;
import com.omarmujcic.timetracking.core.invoices.entity.InvoiceParty;
import com.omarmujcic.timetracking.core.invoices.entity.InvoiceSettings;
import com.omarmujcic.timetracking.core.invoices.entity.InvoiceUserSettings;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

@Mapper(componentModel = "spring", imports = {UUID.class, Math.class})
public interface InvoiceMapper {

    InvoiceParty toParty(InvoicePartyDTO dto);

    InvoicePartyDTO toPartyDTO(InvoiceParty party);

    InvoiceParty copyParty(InvoiceParty party);

    @Mapping(target = "name", source = "displayName")
    @Mapping(target = "contactPerson", ignore = true)
    @Mapping(target = "addressLine1", ignore = true)
    @Mapping(target = "addressLine2", ignore = true)
    @Mapping(target = "postalCode", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "taxId", ignore = true)
    @Mapping(target = "registrationNumber", ignore = true)
    InvoiceParty userToParty(User user);

    @Mapping(target = "name", expression = "java(org.getBillingName() == null || org.getBillingName().isBlank() ? org.getName() : org.getBillingName())")
    @Mapping(target = "contactPerson", source = "billingContactPerson")
    @Mapping(target = "addressLine1", source = "billingAddressLine1")
    @Mapping(target = "addressLine2", source = "billingAddressLine2")
    @Mapping(target = "postalCode", source = "billingPostalCode")
    @Mapping(target = "city", source = "billingCity")
    @Mapping(target = "country", source = "billingCountry")
    @Mapping(target = "email", source = "billingEmail")
    @Mapping(target = "phone", source = "billingPhone")
    @Mapping(target = "taxId", source = "billingTaxId")
    @Mapping(target = "registrationNumber", source = "billingRegistrationNumber")
    InvoiceParty organizationBillingToParty(Organization org);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "joinCode", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "billingName", source = "party.name")
    @Mapping(target = "billingContactPerson", source = "party.contactPerson")
    @Mapping(target = "billingAddressLine1", source = "party.addressLine1")
    @Mapping(target = "billingAddressLine2", source = "party.addressLine2")
    @Mapping(target = "billingPostalCode", source = "party.postalCode")
    @Mapping(target = "billingCity", source = "party.city")
    @Mapping(target = "billingCountry", source = "party.country")
    @Mapping(target = "billingEmail", source = "party.email")
    @Mapping(target = "billingPhone", source = "party.phone")
    @Mapping(target = "billingTaxId", source = "party.taxId")
    @Mapping(target = "billingRegistrationNumber", source = "party.registrationNumber")
    @Mapping(target = "updatedAt", source = "updatedAt")
    void updateOrganizationBilling(InvoicePartyDTO party, OffsetDateTime updatedAt, @MappingTarget Organization organization);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "workspaceType", source = "workspaceType")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "fromParty", ignore = true)
    @Mapping(target = "toParty", source = "toParty")
    @Mapping(target = "nextInvoiceNumber", expression = "java(Math.max(1, request.getNextInvoiceNumber()))")
    @Mapping(target = "taxLabel", source = "request.taxLabel")
    @Mapping(target = "taxRate", expression = "java(scaleDecimal(request.getTaxRate()))")
    @Mapping(target = "terms", source = "request.terms")
    @Mapping(target = "dueDays", expression = "java(Math.max(0, request.getDueDays()))")
    @Mapping(target = "currency", constant = "EUR")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    InvoiceSettings toWorkspaceSettings(InvoiceWorkspaceSettingsRequestDTO request, InvoiceParty toParty,
            WorkspaceType workspaceType,
            User user, Organization organization, OffsetDateTime now);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspaceType", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "fromParty", ignore = true)
    @Mapping(target = "toParty", source = "toParty")
    @Mapping(target = "nextInvoiceNumber", expression = "java(Math.max(1, request.getNextInvoiceNumber()))")
    @Mapping(target = "taxLabel", source = "request.taxLabel")
    @Mapping(target = "taxRate", expression = "java(scaleDecimal(request.getTaxRate()))")
    @Mapping(target = "terms", source = "request.terms")
    @Mapping(target = "dueDays", expression = "java(Math.max(0, request.getDueDays()))")
    @Mapping(target = "updatedAt", source = "now")
    void updateWorkspaceSettings(InvoiceWorkspaceSettingsRequestDTO request, InvoiceParty toParty, OffsetDateTime now,
            @MappingTarget InvoiceSettings settings);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "workspaceType", source = "workspaceType")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "fromParty", source = "request.from")
    @Mapping(target = "nextInvoiceNumber", expression = "java(Math.max(1, request.getNextInvoiceNumber()))")
    @Mapping(target = "taxLabel", source = "request.taxLabel")
    @Mapping(target = "taxRate", expression = "java(scaleDecimal(request.getTaxRate()))")
    @Mapping(target = "terms", source = "request.terms")
    @Mapping(target = "dueDays", expression = "java(Math.max(0, request.getDueDays()))")
    @Mapping(target = "currency", constant = "EUR")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    InvoiceUserSettings toUserSettings(InvoiceUserSettingsRequestDTO request, WorkspaceType workspaceType, User user,
            Organization organization, OffsetDateTime now);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspaceType", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "fromParty", source = "request.from")
    @Mapping(target = "nextInvoiceNumber", expression = "java(Math.max(1, request.getNextInvoiceNumber()))")
    @Mapping(target = "taxLabel", source = "request.taxLabel")
    @Mapping(target = "taxRate", expression = "java(scaleDecimal(request.getTaxRate()))")
    @Mapping(target = "terms", source = "request.terms")
    @Mapping(target = "dueDays", expression = "java(Math.max(0, request.getDueDays()))")
    @Mapping(target = "updatedAt", source = "now")
    void updateUserSettings(InvoiceUserSettingsRequestDTO request, OffsetDateTime now,
            @MappingTarget InvoiceUserSettings settings);

    @Mapping(target = "workspaceType", source = "workspaceType")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "workspaceName", source = "workspaceName")
    @Mapping(target = "from", source = "from")
    @Mapping(target = "to", source = "to")
    @Mapping(target = "nextInvoiceNumber", source = "userSettings.nextInvoiceNumber")
    @Mapping(target = "suggestedInvoiceNumber", source = "suggestedInvoiceNumber")
    @Mapping(target = "taxLabel", source = "userSettings.taxLabel")
    @Mapping(target = "taxRate", source = "userSettings.taxRate")
    @Mapping(target = "terms", source = "userSettings.terms")
    @Mapping(target = "dueDays", source = "userSettings.dueDays")
    @Mapping(target = "currency", source = "userSettings.currency")
    @Mapping(target = "workspaceNextInvoiceNumber", source = "workspaceSettings.nextInvoiceNumber")
    @Mapping(target = "workspaceSuggestedInvoiceNumber", source = "workspaceSuggestedInvoiceNumber")
    @Mapping(target = "workspaceTaxLabel", source = "workspaceSettings.taxLabel")
    @Mapping(target = "workspaceTaxRate", source = "workspaceSettings.taxRate")
    @Mapping(target = "workspaceTerms", source = "workspaceSettings.terms")
    @Mapping(target = "workspaceDueDays", source = "workspaceSettings.dueDays")
    @Mapping(target = "ready", source = "ready")
    @Mapping(target = "fromReady", source = "fromReady")
    @Mapping(target = "toReady", source = "toReady")
    @Mapping(target = "canManageUserSettings", source = "canManageUserSettings")
    @Mapping(target = "canManageWorkspaceSettings", source = "canManageWorkspaceSettings")
    @Mapping(target = "canManageSetup", source = "canManageSetup")
    InvoiceSetupDTO toSetupDTO(InvoiceUserSettings userSettings, InvoiceSettings workspaceSettings,
            InvoicePartyDTO from, InvoicePartyDTO to, WorkspaceType workspaceType, UUID organizationId,
            String workspaceName, boolean ready, boolean fromReady, boolean toReady, boolean canManageUserSettings,
            boolean canManageWorkspaceSettings, boolean canManageSetup, String suggestedInvoiceNumber,
            String workspaceSuggestedInvoiceNumber);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "workspaceType", source = "workspaceType")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "invoiceNumber", source = "invoiceNumber")
    @Mapping(target = "issueDate", source = "issueDate")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "periodStart", source = "periodStart")
    @Mapping(target = "periodEnd", source = "periodEnd")
    @Mapping(target = "fromParty", source = "fromParty")
    @Mapping(target = "toParty", source = "toParty")
    @Mapping(target = "taxLabel", source = "taxLabel")
    @Mapping(target = "taxRate", expression = "java(scaleDecimal(taxRate))")
    @Mapping(target = "subtotal", expression = "java(scaleDecimal(subtotal))")
    @Mapping(target = "taxAmount", expression = "java(scaleDecimal(taxAmount))")
    @Mapping(target = "total", expression = "java(scaleDecimal(total))")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "terms", source = "terms")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lines", ignore = true)
    Invoice toInvoice(WorkspaceType workspaceType, User user, Organization organization, User createdBy,
            String invoiceNumber, LocalDate issueDate, LocalDate dueDate, LocalDate periodStart, LocalDate periodEnd,
            InvoiceParty fromParty, InvoiceParty toParty, String taxLabel, BigDecimal taxRate, BigDecimal subtotal,
            BigDecimal taxAmount, BigDecimal total, String currency, String terms, OffsetDateTime createdAt);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "invoice", source = "invoice")
    @Mapping(target = "lineOrder", source = "lineOrder")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "projectKey", source = "line.projectKey")
    @Mapping(target = "projectName", source = "line.projectName")
    @Mapping(target = "description", source = "line.description")
    @Mapping(target = "durationSeconds", source = "line.durationSeconds")
    @Mapping(target = "quantity", source = "line.quantity")
    @Mapping(target = "unitPrice", source = "line.unitPrice")
    @Mapping(target = "taxRate", expression = "java(scaleDecimal(taxRate))")
    @Mapping(target = "totalAmount", source = "line.totalAmount")
    InvoiceLine toLine(Invoice invoice, Project project, InvoiceWorkLineDTO line, BigDecimal taxRate, int lineOrder);

    @Mapping(target = "projectId", source = "project.id")
    InvoiceLineDTO toLineDTO(InvoiceLine line);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "from", source = "fromParty")
    @Mapping(target = "to", source = "toParty")
    InvoiceDTO toDTO(Invoice invoice);

    InvoiceHistoryItemDTO toHistoryItemDTO(Invoice invoice);

    default BigDecimal scaleDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}
