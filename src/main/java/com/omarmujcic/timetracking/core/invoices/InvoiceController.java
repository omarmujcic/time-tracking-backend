package com.omarmujcic.timetracking.core.invoices;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceGenerateRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceHistoryItemDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceSetupDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceSetupRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceUserSettingsRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceWorkspaceSettingsRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceWorkPreviewDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/setup")
    public InvoiceSetupDTO setup(@AuthenticationPrincipal User user) {
        return invoiceService.setup(user);
    }

    @PutMapping("/setup")
    public InvoiceSetupDTO saveSetup(@AuthenticationPrincipal User user,
            @Valid @RequestBody InvoiceSetupRequestDTO request) {
        return invoiceService.saveSetup(user, request);
    }

    @PutMapping("/setup/user")
    public InvoiceSetupDTO saveUserSettings(@AuthenticationPrincipal User user,
            @Valid @RequestBody InvoiceUserSettingsRequestDTO request) {
        return invoiceService.saveUserSettings(user, request);
    }

    @PutMapping("/setup/workspace")
    public InvoiceSetupDTO saveWorkspaceSettings(@AuthenticationPrincipal User user,
            @Valid @RequestBody InvoiceWorkspaceSettingsRequestDTO request) {
        return invoiceService.saveWorkspaceSettings(user, request);
    }

    @GetMapping("/work-preview")
    public InvoiceWorkPreviewDTO workPreview(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "UTC") String timezone,
            @RequestParam(required = false) List<String> projectKeys
    ) {
        return invoiceService.workPreview(user, startDate, endDate, timezone, projectKeys);
    }

    @PostMapping("/generate")
    public InvoiceDTO generate(@AuthenticationPrincipal User user,
            @Valid @RequestBody InvoiceGenerateRequestDTO request) {
        return invoiceService.generate(user, request);
    }

    @GetMapping("/history")
    public List<InvoiceHistoryItemDTO> history(@AuthenticationPrincipal User user) {
        return invoiceService.history(user);
    }

    @GetMapping("/{id}")
    public InvoiceDTO invoice(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return invoiceService.invoice(user, id);
    }
}
