package com.omarmujcic.timetracking.core.invoices.dto;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceUserSettingsRequestDTO {

    @Valid
    @NotNull
    private InvoicePartyDTO from;

    @Min(1)
    private int nextInvoiceNumber;

    @NotBlank
    @Size(max = 80)
    private String taxLabel;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal taxRate;

    @Size(max = 5000)
    private String terms;

    @Min(0)
    private int dueDays;
}
