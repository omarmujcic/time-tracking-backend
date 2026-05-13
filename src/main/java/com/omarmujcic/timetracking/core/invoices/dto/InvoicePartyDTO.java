package com.omarmujcic.timetracking.core.invoices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoicePartyDTO {

    @Size(max = 160)
    private String name;

    @Size(max = 160)
    private String contactPerson;

    @Size(max = 220)
    private String addressLine1;

    @Size(max = 220)
    private String addressLine2;

    @Size(max = 40)
    private String postalCode;

    @Size(max = 120)
    private String city;

    @Size(max = 120)
    private String country;

    @Email
    @Size(max = 254)
    private String email;

    @Size(max = 40)
    private String phone;

    @Size(max = 80)
    private String taxId;

    @Size(max = 80)
    private String registrationNumber;
}
