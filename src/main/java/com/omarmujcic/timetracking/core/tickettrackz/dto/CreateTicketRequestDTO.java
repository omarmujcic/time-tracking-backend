package com.omarmujcic.timetracking.core.tickettrackz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.omarmujcic.timetracking.core.tickettrackz.entity.TicketPriority;
import com.omarmujcic.timetracking.core.tickettrackz.entity.TicketStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTicketRequestDTO {

    @NotNull
    private UUID projectId;

    private UUID assigneeUserId;

    @NotBlank
    @Size(max = 160)
    private String title;

    @Size(max = 10000)
    private String description;

    @Size(max = 10000)
    private String productContext;

    @NotNull
    private TicketStatus status;

    private TicketPriority priority;

    private LocalDate dueDate;

    @PositiveOrZero
    @Max(999)
    private Integer storyPoints;

    @DecimalMin("0.00")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal estimatedHours;
}
