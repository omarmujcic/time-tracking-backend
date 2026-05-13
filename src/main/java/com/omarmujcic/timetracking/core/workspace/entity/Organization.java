package com.omarmujcic.timetracking.core.workspace.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.auth.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
public class Organization {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "join_code", nullable = false, unique = true, length = 24)
    private String joinCode;

    @Column(name = "billing_name", length = 160)
    private String billingName;

    @Column(name = "billing_contact_person", length = 160)
    private String billingContactPerson;

    @Column(name = "billing_address_line_1", length = 220)
    private String billingAddressLine1;

    @Column(name = "billing_address_line_2", length = 220)
    private String billingAddressLine2;

    @Column(name = "billing_postal_code", length = 40)
    private String billingPostalCode;

    @Column(name = "billing_city", length = 120)
    private String billingCity;

    @Column(name = "billing_country", length = 120)
    private String billingCountry;

    @Column(name = "billing_email", length = 254)
    private String billingEmail;

    @Column(name = "billing_phone", length = 40)
    private String billingPhone;

    @Column(name = "billing_tax_id", length = 80)
    private String billingTaxId;

    @Column(name = "billing_registration_number", length = 80)
    private String billingRegistrationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
