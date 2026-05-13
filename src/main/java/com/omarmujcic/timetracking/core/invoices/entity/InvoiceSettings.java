package com.omarmujcic.timetracking.core.invoices.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoice_settings")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceSettings {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "workspace_type", nullable = false, length = 20)
    private WorkspaceType workspaceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "from_name", length = 160)),
        @AttributeOverride(name = "contactPerson", column = @Column(name = "from_contact_person", length = 160)),
        @AttributeOverride(name = "addressLine1", column = @Column(name = "from_address_line_1", length = 220)),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "from_address_line_2", length = 220)),
        @AttributeOverride(name = "postalCode", column = @Column(name = "from_postal_code", length = 40)),
        @AttributeOverride(name = "city", column = @Column(name = "from_city", length = 120)),
        @AttributeOverride(name = "country", column = @Column(name = "from_country", length = 120)),
        @AttributeOverride(name = "email", column = @Column(name = "from_email", length = 254)),
        @AttributeOverride(name = "phone", column = @Column(name = "from_phone", length = 40)),
        @AttributeOverride(name = "taxId", column = @Column(name = "from_tax_id", length = 80)),
        @AttributeOverride(name = "registrationNumber", column = @Column(name = "from_registration_number", length = 80))
    })
    private InvoiceParty fromParty;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "to_name", length = 160)),
        @AttributeOverride(name = "contactPerson", column = @Column(name = "to_contact_person", length = 160)),
        @AttributeOverride(name = "addressLine1", column = @Column(name = "to_address_line_1", length = 220)),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "to_address_line_2", length = 220)),
        @AttributeOverride(name = "postalCode", column = @Column(name = "to_postal_code", length = 40)),
        @AttributeOverride(name = "city", column = @Column(name = "to_city", length = 120)),
        @AttributeOverride(name = "country", column = @Column(name = "to_country", length = 120)),
        @AttributeOverride(name = "email", column = @Column(name = "to_email", length = 254)),
        @AttributeOverride(name = "phone", column = @Column(name = "to_phone", length = 40)),
        @AttributeOverride(name = "taxId", column = @Column(name = "to_tax_id", length = 80)),
        @AttributeOverride(name = "registrationNumber", column = @Column(name = "to_registration_number", length = 80))
    })
    private InvoiceParty toParty;

    @Column(name = "next_invoice_number", nullable = false)
    private int nextInvoiceNumber;

    @Column(name = "tax_label", nullable = false, length = 80)
    private String taxLabel;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(columnDefinition = "text")
    private String terms;

    @Column(name = "due_days", nullable = false)
    private int dueDays;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
