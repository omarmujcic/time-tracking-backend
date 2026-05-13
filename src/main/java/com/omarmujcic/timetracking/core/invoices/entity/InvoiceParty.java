package com.omarmujcic.timetracking.core.invoices.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class InvoiceParty {

    @Column(length = 160)
    private String name;

    @Column(name = "contact_person", length = 160)
    private String contactPerson;

    @Column(name = "address_line_1", length = 220)
    private String addressLine1;

    @Column(name = "address_line_2", length = 220)
    private String addressLine2;

    @Column(name = "postal_code", length = 40)
    private String postalCode;

    @Column(length = 120)
    private String city;

    @Column(length = 120)
    private String country;

    @Column(length = 254)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(name = "tax_id", length = 80)
    private String taxId;

    @Column(name = "registration_number", length = 80)
    private String registrationNumber;
}
