package com.omarmujcic.timetracking.core.invoices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.auth.repository.UserRepository;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceGenerateRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceHistoryItemDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoicePartyDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceSetupDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceSetupRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceUserSettingsRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceWorkspaceSettingsRequestDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceWorkLineDTO;
import com.omarmujcic.timetracking.core.invoices.dto.InvoiceWorkPreviewDTO;
import com.omarmujcic.timetracking.core.invoices.entity.Invoice;
import com.omarmujcic.timetracking.core.invoices.entity.InvoiceParty;
import com.omarmujcic.timetracking.core.invoices.entity.InvoiceSettings;
import com.omarmujcic.timetracking.core.invoices.entity.InvoiceUserSettings;
import com.omarmujcic.timetracking.core.invoices.mapper.InvoiceMapper;
import com.omarmujcic.timetracking.core.invoices.repository.InvoiceRepository;
import com.omarmujcic.timetracking.core.invoices.repository.InvoiceSettingsRepository;
import com.omarmujcic.timetracking.core.invoices.repository.InvoiceUserSettingsRepository;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;
import com.omarmujcic.timetracking.core.timetracking.repository.TimeEntryRepository;
import com.omarmujcic.timetracking.core.workspace.WorkspaceService;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final String CURRENCY = "EUR";
    private static final BigDecimal ONE = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final String LEGACY_PROJECT_PREFIX = "name:";

    private final UserRepository userRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final WorkspaceService workspaceService;
    private final InvoiceSettingsRepository settingsRepository;
    private final InvoiceUserSettingsRepository userSettingsRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    @Transactional(readOnly = true)
    public InvoiceSetupDTO setup(User user) {
        WorkspaceContext context = workspaceContext(user);
        InvoiceSettings workspaceSettings = settings(context);
        InvoiceUserSettings userSettings = userSettings(context, workspaceSettings);
        return toSetupDTO(userSettings, workspaceSettings, context);
    }

    @Transactional
    public InvoiceSetupDTO saveSetup(User user, InvoiceSetupRequestDTO request) {
        saveUserSettings(user, userSettingsRequest(request));
        return saveWorkspaceSettings(user, workspaceSettingsRequest(request));
    }

    @Transactional
    public InvoiceSetupDTO saveUserSettings(User user, InvoiceUserSettingsRequestDTO request) {
        WorkspaceContext context = workspaceContext(user);
        OffsetDateTime now = now();
        InvoiceSettings workspaceSettings = settings(context);
        InvoiceUserSettings saved = persistedUserSettings(context);
        if (saved == null) {
            saved = invoiceMapper.toUserSettings(request, context.type(), context.user(), context.organization(), now);
        } else {
            invoiceMapper.updateUserSettings(request, now, saved);
        }
        saved = userSettingsRepository.save(saved);
        return toSetupDTO(saved, workspaceSettings, context);
    }

    @Transactional
    public InvoiceSetupDTO saveWorkspaceSettings(User user, InvoiceWorkspaceSettingsRequestDTO request) {
        WorkspaceContext context = workspaceContext(user);
        assertCanManageWorkspaceSettings(context);

        OffsetDateTime now = now();
        InvoiceParty toParty = invoiceMapper.toParty(request.getTo());
        if (context.organization() != null) {
            invoiceMapper.updateOrganizationBilling(request.getTo(), now, context.organization());
            toParty = invoiceMapper.organizationBillingToParty(context.organization());
        }

        InvoiceSettings saved = persistedSettings(context);
        if (saved == null) {
            saved = invoiceMapper.toWorkspaceSettings(request, toParty, context.type(),
                    context.type() == WorkspaceType.PERSONAL
                    ? context.user()
                    : null, context.organization(), now);
        } else {
            invoiceMapper.updateWorkspaceSettings(request, toParty, now, saved);
        }
        saved = settingsRepository.save(saved);
        InvoiceUserSettings userSettings = userSettings(context, saved);
        return toSetupDTO(userSettings, saved, context);
    }

    @Transactional(readOnly = true)
    public InvoiceWorkPreviewDTO workPreview(User user, LocalDate startDate, LocalDate endDate, String timezone,
            List<String> projectKeys) {
        WorkspaceContext context = workspaceContext(user);
        return workPreview(context, startDate, endDate, timezone, projectKeys);
    }

    @Transactional
    public InvoiceDTO generate(User user, InvoiceGenerateRequestDTO request) {
        WorkspaceContext context = workspaceContext(user);
        InvoiceSettings workspaceSettings = settings(context);
        InvoiceUserSettings userSettings = userSettings(context, workspaceSettings);

        InvoiceSetupDTO setup = toSetupDTO(userSettings, workspaceSettings, context);
        if (!setup.ready()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice setup is incomplete");
        }
        if (request.getDueDate().isBefore(request.getIssueDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date cannot be before issue date");
        }

        InvoiceWorkPreviewDTO preview = workPreview(context, request.getStartDate(), request.getEndDate(),
                request.getTimezone(), request.getProjectKeys());
        if (preview.lines().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No completed work matches this invoice");
        }

        String invoiceNumber = request.getInvoiceNumber().trim();
        BigDecimal taxRate = scaleTax(request.getTaxRate());
        BigDecimal subtotal = preview.subtotal();
        BigDecimal taxAmount = subtotal.multiply(taxRate)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        InvoiceParty toParty = activeToParty(workspaceSettings, context);
        OffsetDateTime createdAt = now();

        Invoice invoice = invoiceMapper.toInvoice(
                context.type(),
                context.type() == WorkspaceType.PERSONAL ? context.user() : null,
                context.organization(),
                context.user(),
                invoiceNumber,
                request.getIssueDate(),
                request.getDueDate(),
                request.getStartDate(),
                request.getEndDate(),
                invoiceMapper.copyParty(userSettings.getFromParty()),
                invoiceMapper.copyParty(toParty),
                request.getTaxLabel().trim(),
                taxRate,
                subtotal,
                taxAmount,
                total,
                CURRENCY,
                normalizeOptional(request.getTerms()),
                createdAt
        );

        Map<String, Project> projectsByKey = projectsByKey(context);
        int lineOrder = 1;
        for (InvoiceWorkLineDTO line : preview.lines()) {
            invoice.getLines().add(invoiceMapper.toLine(invoice, projectsByKey.get(line.projectKey()), line, taxRate,
                    lineOrder++));
        }

        if (invoiceNumber.equals(suggestedInvoiceNumber(userSettings))) {
            userSettings.setNextInvoiceNumber(userSettings.getNextInvoiceNumber() + 1);
            userSettings.setUpdatedAt(createdAt);
            userSettingsRepository.save(userSettings);
        }

        return invoiceMapper.toDTO(invoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public List<InvoiceHistoryItemDTO> history(User user) {
        WorkspaceContext context = workspaceContext(user);
        List<Invoice> invoices = context.type() == WorkspaceType.PERSONAL
                ? invoiceRepository.findTop20ByUserIdOrderByCreatedAtDesc(context.user().getId())
                : invoiceRepository.findTop20ByOrganizationIdOrderByCreatedAtDesc(context.organization().getId());
        return invoices.stream()
            .map(invoiceMapper::toHistoryItemDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceDTO invoice(User user, UUID invoiceId) {
        WorkspaceContext context = workspaceContext(user);
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        if (!canAccessInvoice(context, invoice)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found");
        }
        return invoiceMapper.toDTO(invoice);
    }

    private InvoiceWorkPreviewDTO workPreview(WorkspaceContext context, LocalDate startDate, LocalDate endDate,
            String timezone, List<String> projectKeys) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        ZoneId zone = zone(timezone);
        Instant rangeStart = startDate.atStartOfDay(zone).toInstant();
        Instant rangeEnd = endDate.plusDays(1).atStartOfDay(zone).toInstant();
        Set<String> selectedProjectKeys = selectedProjectKeys(projectKeys);
        Map<String, ProjectTotals> totalsByProject = new LinkedHashMap<>();

        for (TimeEntry entry : visibleEntries(context)) {
            if (entry.getEndedAt() == null) {
                continue;
            }
            String key = projectKey(entry);
            if (!selectedProjectKeys.isEmpty() && !selectedProjectKeys.contains(key)) {
                continue;
            }
            long seconds = overlapSeconds(entry.getStartedAt().toInstant(), entry.getEndedAt().toInstant(),
                    rangeStart, rangeEnd);
            if (seconds <= 0) {
                continue;
            }
            ProjectTotals totals = totalsByProject.computeIfAbsent(key,
                    ignored -> new ProjectTotals(key, entry.getProject(), projectName(entry)));
            totals.add(seconds, amount(entry.getHourlyRate(), seconds));
        }

        List<InvoiceWorkLineDTO> lines = totalsByProject.values().stream()
            .sorted(Comparator.comparingLong(ProjectTotals::seconds).reversed()
                .thenComparing(ProjectTotals::projectName, String.CASE_INSENSITIVE_ORDER))
            .map(totals -> new InvoiceWorkLineDTO(
                    totals.projectKey(),
                    totals.project() == null ? null : totals.project().getId(),
                    totals.projectName(),
                    durationDescription(totals.seconds()),
                    totals.seconds(),
                    ONE,
                    scaleMoney(totals.amount()),
                    scaleMoney(totals.amount()),
                    CURRENCY
            ))
            .toList();
        BigDecimal subtotal = lines.stream()
            .map(InvoiceWorkLineDTO::totalAmount)
            .reduce(ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        return new InvoiceWorkPreviewDTO(startDate, endDate, lines, subtotal, CURRENCY);
    }

    private InvoiceSetupDTO toSetupDTO(InvoiceUserSettings userSettings, InvoiceSettings workspaceSettings,
            WorkspaceContext context) {
        InvoiceParty fromParty = userSettings.getFromParty() == null ? invoiceMapper.userToParty(context.user())
                : userSettings.getFromParty();
        InvoiceParty toParty = activeToParty(workspaceSettings, context);
        InvoicePartyDTO from = invoiceMapper.toPartyDTO(fromParty);
        InvoicePartyDTO to = invoiceMapper.toPartyDTO(toParty);
        boolean fromReady = partyReady(from);
        boolean toReady = partyReady(to);
        boolean canManageWorkspaceSettings = canManageWorkspaceSettings(context);
        return invoiceMapper.toSetupDTO(userSettings, workspaceSettings, from, to, context.type(),
                context.organization() == null ? null : context.organization().getId(), workspaceName(context),
                fromReady && toReady, fromReady, toReady, true, canManageWorkspaceSettings,
                canManageWorkspaceSettings, suggestedInvoiceNumber(userSettings),
                suggestedInvoiceNumber(workspaceSettings));
    }

    private InvoiceSettings settings(WorkspaceContext context) {
        InvoiceSettings settings = persistedSettings(context);
        return settings == null ? defaultSettings(context) : settings;
    }

    private InvoiceUserSettings userSettings(WorkspaceContext context, InvoiceSettings workspaceSettings) {
        InvoiceUserSettings userSettings = persistedUserSettings(context);
        return userSettings == null ? defaultUserSettings(context, workspaceSettings) : userSettings;
    }

    private InvoiceSettings persistedSettings(WorkspaceContext context) {
        if (context.type() == WorkspaceType.ORGANIZATION) {
            return settingsRepository.findByOrganizationId(context.organization().getId()).orElse(null);
        }
        return settingsRepository.findByUserId(context.user().getId()).orElse(null);
    }

    private InvoiceUserSettings persistedUserSettings(WorkspaceContext context) {
        if (context.type() == WorkspaceType.ORGANIZATION) {
            return userSettingsRepository.findByUserIdAndOrganizationId(context.user().getId(),
                    context.organization().getId()).orElse(null);
        }
        return userSettingsRepository.findByUserIdAndWorkspaceTypeAndOrganizationIsNull(context.user().getId(),
                WorkspaceType.PERSONAL).orElse(null);
    }

    private InvoiceSettings defaultSettings(WorkspaceContext context) {
        InvoiceSettings settings = new InvoiceSettings();
        OffsetDateTime now = now();
        settings.setWorkspaceType(context.type());
        settings.setUser(context.type() == WorkspaceType.PERSONAL ? context.user() : null);
        settings.setOrganization(context.organization());
        settings.setFromParty(invoiceMapper.userToParty(context.user()));
        settings.setToParty(activeToParty(settings, context));
        settings.setNextInvoiceNumber(1);
        settings.setTaxLabel("Tax");
        settings.setTaxRate(ZERO);
        settings.setDueDays(14);
        settings.setCurrency(CURRENCY);
        settings.setCreatedAt(now);
        settings.setUpdatedAt(now);
        return settings;
    }

    private InvoiceUserSettings defaultUserSettings(WorkspaceContext context, InvoiceSettings workspaceSettings) {
        InvoiceUserSettings settings = new InvoiceUserSettings();
        OffsetDateTime now = now();
        settings.setId(UUID.randomUUID());
        settings.setWorkspaceType(context.type());
        settings.setUser(context.user());
        settings.setOrganization(context.organization());
        settings.setFromParty(invoiceMapper.userToParty(context.user()));
        settings.setNextInvoiceNumber(Math.max(1, workspaceSettings.getNextInvoiceNumber()));
        settings.setTaxLabel(workspaceSettings.getTaxLabel());
        settings.setTaxRate(workspaceSettings.getTaxRate());
        settings.setTerms(workspaceSettings.getTerms());
        settings.setDueDays(workspaceSettings.getDueDays());
        settings.setCurrency(CURRENCY);
        settings.setCreatedAt(now);
        settings.setUpdatedAt(now);
        return settings;
    }

    private InvoiceParty activeToParty(InvoiceSettings settings, WorkspaceContext context) {
        if (context.organization() != null) {
            return invoiceMapper.organizationBillingToParty(context.organization());
        }
        if (settings.getToParty() != null) {
            return settings.getToParty();
        }
        return new InvoiceParty();
    }

    private boolean partyReady(InvoicePartyDTO party) {
        if (party == null) {
            return false;
        }
        return hasText(party.getName())
            && hasText(party.getAddressLine1())
            && hasText(party.getPostalCode())
            && hasText(party.getCity())
            && hasText(party.getCountry())
            && hasText(party.getEmail());
    }

    private WorkspaceContext workspaceContext(User user) {
        User managed = userRepository.findById(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (managed.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION) {
            OrganizationMember member = workspaceService.activeOrganizationMembership(managed);
            return new WorkspaceContext(WorkspaceType.ORGANIZATION, managed, member.getOrganization(), member);
        }
        return new WorkspaceContext(WorkspaceType.PERSONAL, managed, null, null);
    }

    private List<TimeEntry> visibleEntries(WorkspaceContext context) {
        if (context.type() == WorkspaceType.ORGANIZATION) {
            List<TimeEntry> entries = timeEntryRepository.findByOrganizationIdOrderByStartedAtDesc(
                    context.organization().getId());
            if (context.member().getRole() == OrganizationRole.MEMBER) {
                return entries.stream().filter(entry -> entry.getUser().getId().equals(context.user().getId())).toList();
            }
            return entries;
        }
        return timeEntryRepository.findByUserIdOrderByStartedAtDesc(context.user().getId()).stream()
            .filter(entry -> entry.getWorkspaceType() == null || entry.getWorkspaceType() == WorkspaceType.PERSONAL)
            .toList();
    }

    private Map<String, Project> projectsByKey(WorkspaceContext context) {
        Map<String, Project> projects = new LinkedHashMap<>();
        for (TimeEntry entry : visibleEntries(context)) {
            if (entry.getProject() != null) {
                projects.putIfAbsent(projectKey(entry), entry.getProject());
            }
        }
        return projects;
    }

    private void assertCanManageWorkspaceSettings(WorkspaceContext context) {
        if (!canManageWorkspaceSettings(context)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invoice setup manager access required");
        }
    }

    private boolean canManageWorkspaceSettings(WorkspaceContext context) {
        return context.type() == WorkspaceType.PERSONAL || workspaceService.canManage(context.member().getRole());
    }

    private boolean canAccessInvoice(WorkspaceContext context, Invoice invoice) {
        if (context.type() == WorkspaceType.PERSONAL) {
            return invoice.getWorkspaceType() == WorkspaceType.PERSONAL
                && invoice.getUser() != null
                && invoice.getUser().getId().equals(context.user().getId());
        }
        return invoice.getWorkspaceType() == WorkspaceType.ORGANIZATION
            && invoice.getOrganization() != null
            && invoice.getOrganization().getId().equals(context.organization().getId());
    }

    private String workspaceName(WorkspaceContext context) {
        return context.organization() == null ? "Personal" : context.organization().getName();
    }

    private ZoneId zone(String timezone) {
        try {
            return ZoneId.of(timezone == null || timezone.isBlank() ? "UTC" : timezone);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timezone is invalid");
        }
    }

    private Set<String> selectedProjectKeys(List<String> projectKeys) {
        if (projectKeys == null) {
            return Set.of();
        }
        return projectKeys.stream()
            .filter(this::hasText)
            .map(String::trim)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private String projectKey(TimeEntry entry) {
        if (entry.getProject() != null) {
            return entry.getProject().getId().toString();
        }
        return LEGACY_PROJECT_PREFIX + projectName(entry).trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String projectName(TimeEntry entry) {
        return entry.getProject() == null ? entry.getProjectName() : entry.getProject().getName();
    }

    private long overlapSeconds(Instant start, Instant end, Instant rangeStart, Instant rangeEnd) {
        Instant effectiveStart = start.isBefore(rangeStart) ? rangeStart : start;
        Instant effectiveEnd = end.isAfter(rangeEnd) ? rangeEnd : end;
        if (!effectiveEnd.isAfter(effectiveStart)) {
            return 0;
        }
        return Duration.between(effectiveStart, effectiveEnd).toSeconds();
    }

    private BigDecimal amount(BigDecimal hourlyRate, long seconds) {
        return hourlyRate
            .multiply(BigDecimal.valueOf(seconds))
            .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleTax(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String suggestedInvoiceNumber(InvoiceSettings settings) {
        return String.format("%04d", Math.max(1, settings.getNextInvoiceNumber()));
    }

    private String suggestedInvoiceNumber(InvoiceUserSettings settings) {
        return String.format("%04d", Math.max(1, settings.getNextInvoiceNumber()));
    }

    private String durationDescription(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours == 0) {
            return minutes + "m of work";
        }
        return hours + "h" + String.format("%02d", minutes) + "m of work";
    }

    private InvoiceUserSettingsRequestDTO userSettingsRequest(InvoiceSetupRequestDTO request) {
        InvoiceUserSettingsRequestDTO userRequest = new InvoiceUserSettingsRequestDTO();
        userRequest.setFrom(request.getFrom());
        userRequest.setNextInvoiceNumber(request.getNextInvoiceNumber());
        userRequest.setTaxLabel(request.getTaxLabel());
        userRequest.setTaxRate(request.getTaxRate());
        userRequest.setTerms(request.getTerms());
        userRequest.setDueDays(request.getDueDays());
        return userRequest;
    }

    private InvoiceWorkspaceSettingsRequestDTO workspaceSettingsRequest(InvoiceSetupRequestDTO request) {
        InvoiceWorkspaceSettingsRequestDTO workspaceRequest = new InvoiceWorkspaceSettingsRequestDTO();
        workspaceRequest.setTo(request.getTo());
        workspaceRequest.setNextInvoiceNumber(request.getNextInvoiceNumber());
        workspaceRequest.setTaxLabel(request.getTaxLabel());
        workspaceRequest.setTaxRate(request.getTaxRate());
        workspaceRequest.setTerms(request.getTerms());
        workspaceRequest.setDueDays(request.getDueDays());
        return workspaceRequest;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private record WorkspaceContext(
            WorkspaceType type,
            User user,
            Organization organization,
            OrganizationMember member
    ) {
    }

    private static final class ProjectTotals {
        private final String projectKey;
        private final Project project;
        private final String projectName;
        private long seconds;
        private BigDecimal amount = ZERO;

        private ProjectTotals(String projectKey, Project project, String projectName) {
            this.projectKey = projectKey;
            this.project = project;
            this.projectName = projectName;
        }

        private void add(long seconds, BigDecimal amount) {
            this.seconds += seconds;
            this.amount = this.amount.add(amount).setScale(2, RoundingMode.HALF_UP);
        }

        private String projectKey() {
            return projectKey;
        }

        private Project project() {
            return project;
        }

        private String projectName() {
            return projectName;
        }

        private long seconds() {
            return seconds;
        }

        private BigDecimal amount() {
            return amount;
        }
    }
}
