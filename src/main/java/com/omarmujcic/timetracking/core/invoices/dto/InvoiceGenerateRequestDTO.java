package com.omarmujcic.timetracking.core.invoices.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceGenerateRequestDTO {

    @NotBlank
    @Size(max = 80)
    private String invoiceNumber;

    @NotNull
    private LocalDate issueDate;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotBlank
    @Size(max = 80)
    private String timezone;

    @NotEmpty
    private List<@NotBlank @Size(max = 180) String> projectKeys;

    @NotBlank
    @Size(max = 80)
    private String taxLabel;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal taxRate;

    @Size(max = 5000)
    private String terms;
}
