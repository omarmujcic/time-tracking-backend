package com.omarmujcic.timetracking.core.tickettrackz;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.tickettrackz.dto.CreateTicketRequestDTO;
import com.omarmujcic.timetracking.core.tickettrackz.dto.TicketDTO;
import com.omarmujcic.timetracking.core.tickettrackz.dto.UpdateTicketRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-trackz/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public List<TicketDTO> list(@AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "false") boolean assignedToMe) {
        return ticketService.list(user, assignedToMe);
    }

    @GetMapping("/{idOrKey}")
    public TicketDTO find(@AuthenticationPrincipal User user, @PathVariable String idOrKey) {
        return ticketService.find(user, idOrKey);
    }

    @PostMapping
    public TicketDTO create(@AuthenticationPrincipal User user, @Valid @RequestBody CreateTicketRequestDTO request) {
        return ticketService.create(user, request);
    }

    @PutMapping("/{idOrKey}")
    public TicketDTO update(@AuthenticationPrincipal User user, @PathVariable String idOrKey,
            @Valid @RequestBody UpdateTicketRequestDTO request) {
        return ticketService.update(user, idOrKey, request);
    }
}
