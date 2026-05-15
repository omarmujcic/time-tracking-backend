package com.omarmujcic.timetracking.core.billing;

import static com.omarmujcic.timetracking.core.billing.BillingConstants.CURRENCY;
import static com.omarmujcic.timetracking.core.billing.BillingConstants.LEGACY_PROJECT_PREFIX;
import static com.omarmujcic.timetracking.core.billing.BillingConstants.MONTH_LABEL;
import static com.omarmujcic.timetracking.core.billing.BillingConstants.ONE;
import static com.omarmujcic.timetracking.core.billing.BillingConstants.ZERO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.projects.entity.ProjectBillingRule;
import com.omarmujcic.timetracking.core.projects.entity.ProjectBillingRuleType;
import com.omarmujcic.timetracking.core.projects.entity.ProjectStatus;
import com.omarmujcic.timetracking.core.projects.repository.ProjectBillingRuleRepository;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillingCalculationService {

    private final ProjectBillingRuleRepository billingRuleRepository;

    public BillingReportResult reportTotals(List<TimeEntry> entries, List<Project> candidateProjects,
            LocalDate startDate, LocalDate endDate, ZoneId zone, boolean includeEmptyRetainers) {
        Calculation calculation = calculate(entries, candidateProjects, Set.of(), startDate, endDate, zone,
                includeEmptyRetainers);
        List<BillingProjectTotal> projectTotals = calculation.projectTotals().values().stream()
            .sorted(Comparator.comparingLong(ProjectAccumulator::seconds).reversed()
                .thenComparing(ProjectAccumulator::projectName, String.CASE_INSENSITIVE_ORDER))
            .map(total -> new BillingProjectTotal(total.projectName(), total.seconds(), scaleMoney(total.amount())))
            .toList();
        BigDecimal totalAmount = projectTotals.stream()
            .map(BillingProjectTotal::totalAmount)
            .reduce(ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        return new BillingReportResult(totalAmount, projectTotals);
    }

    public List<BillingLine> invoiceLines(List<TimeEntry> entries, List<Project> candidateProjects,
            Set<String> selectedProjectKeys, LocalDate startDate, LocalDate endDate, ZoneId zone) {
        Calculation calculation = calculate(entries, candidateProjects, selectedProjectKeys, startDate, endDate, zone,
                true);
        List<BillingLine> lines = new ArrayList<>();

        calculation.partialTotals().values().stream()
            .filter(total -> total.seconds() > 0)
            .sorted(projectComparator())
            .forEach(total -> lines.add(new BillingLine(
                    total.projectKey(),
                    total.projectId(),
                    total.project(),
                    total.projectName(),
                    durationDescription(total.seconds()) + " of work",
                    total.seconds(),
                    ONE,
                    scaleMoney(total.amount()),
                    scaleMoney(total.amount()),
                    CURRENCY
            )));

        calculation.monthTotals().values().stream()
            .sorted(Comparator.comparing(ProjectMonthTotals::month)
                .thenComparing(ProjectMonthTotals::projectName, String.CASE_INSENSITIVE_ORDER))
            .forEach(total -> addMonthInvoiceLines(lines, total));

        return lines;
    }

    private Calculation calculate(List<TimeEntry> entries, List<Project> candidateProjects, Set<String> selectedProjectKeys,
            LocalDate startDate, LocalDate endDate, ZoneId zone, boolean includeEmptyRetainers) {
        List<YearMonth> fullMonths = fullMonths(startDate, endDate);
        Map<String, Project> candidateProjectsByKey = candidateProjects.stream()
            .collect(Collectors.toMap(this::projectKey, project -> project, (first, second) -> first, LinkedHashMap::new));
        Map<UUID, List<ProjectBillingRule>> rulesByProject = rulesByProject(candidateProjects);
        Map<String, ProjectAccumulator> projectTotals = new LinkedHashMap<>();
        Map<String, ProjectAccumulator> partialTotals = new LinkedHashMap<>();
        Map<ProjectMonthKey, ProjectMonthTotals> monthTotals = new LinkedHashMap<>();
        Instant rangeStart = startDate.atStartOfDay(zone).toInstant();
        Instant rangeEnd = endDate.plusDays(1).atStartOfDay(zone).toInstant();

        for (TimeEntry entry : entries) {
            if (entry.getEndedAt() == null) {
                continue;
            }
            String key = projectKey(entry);
            if (!selectedProjectKeys.isEmpty() && !selectedProjectKeys.contains(key)) {
                continue;
            }
            long totalSeconds = overlapSeconds(entry.getStartedAt().toInstant(), entry.getEndedAt().toInstant(),
                    rangeStart, rangeEnd);
            if (totalSeconds <= 0) {
                continue;
            }

            BigDecimal totalRawAmount = rawAmount(entry, totalSeconds);
            long fullMonthSeconds = 0;
            BigDecimal fullMonthRawAmount = ZERO;
            for (YearMonth month : fullMonths) {
                MonthWindow window = monthWindow(month, zone);
                long seconds = overlapSeconds(entry.getStartedAt().toInstant(), entry.getEndedAt().toInstant(),
                        window.start(), window.end());
                if (seconds <= 0) {
                    continue;
                }
                BigDecimal rawAmount = rawAmount(entry, seconds);
                ProjectMonthTotals totals = monthTotals.computeIfAbsent(
                        new ProjectMonthKey(key, month),
                        ignored -> new ProjectMonthTotals(key, entry.getProject(), projectName(entry), month)
                );
                totals.add(seconds, rawAmount);
                fullMonthSeconds += seconds;
                fullMonthRawAmount = fullMonthRawAmount.add(rawAmount).setScale(2, RoundingMode.HALF_UP);
            }

            long partialSeconds = Math.max(0, totalSeconds - fullMonthSeconds);
            BigDecimal partialAmount = totalRawAmount.subtract(fullMonthRawAmount).setScale(2, RoundingMode.HALF_UP);
            if (partialAmount.compareTo(ZERO) < 0) {
                partialAmount = ZERO;
            }
            if (partialSeconds > 0 || partialAmount.compareTo(ZERO) > 0) {
                ProjectAccumulator partial = partialTotals.computeIfAbsent(key,
                        ignored -> new ProjectAccumulator(key, entry.getProject(), projectName(entry)));
                partial.add(partialSeconds, partialAmount);
            }
        }

        if (includeEmptyRetainers && !fullMonths.isEmpty()) {
            for (Project project : candidateProjectsByKey.values()) {
                if (project.getStatus() != ProjectStatus.ACTIVE) {
                    continue;
                }
                if (!selectedProjectKeys.isEmpty() && !selectedProjectKeys.contains(projectKey(project))) {
                    continue;
                }
                for (YearMonth month : fullMonths) {
                    ProjectMonthKey key = new ProjectMonthKey(projectKey(project), month);
                    ProjectBillingRule rule = activeRule(project, month, rulesByProject);
                    if (isRetainerRule(rule)) {
                        monthTotals.computeIfAbsent(key,
                                ignored -> new ProjectMonthTotals(projectKey(project), project, project.getName(), month));
                    }
                }
            }
        }

        partialTotals.values().forEach(total -> projectTotals
            .computeIfAbsent(total.projectKey(), ignored -> new ProjectAccumulator(total.projectKey(), total.project(),
                    total.projectName()))
            .add(total.seconds(), total.amount()));

        monthTotals.values().forEach(total -> {
            ProjectBillingRule rule = activeRule(total.project(), total.month(), rulesByProject);
            BigDecimal amount = monthAmount(rule, total.rawAmount(), total.seconds());
            total.setBillingAmount(amount);
            projectTotals
                .computeIfAbsent(total.projectKey(), ignored -> new ProjectAccumulator(total.projectKey(),
                        total.project(), total.projectName()))
                .add(total.seconds(), amount);
        });

        return new Calculation(projectTotals, partialTotals, monthTotals);
    }

    private void addMonthInvoiceLines(List<BillingLine> lines, ProjectMonthTotals total) {
        ProjectBillingRule rule = total.project() == null ? null : activeRule(total.project(), total.month(), null);
        String month = MONTH_LABEL.format(total.month());
        if (rule == null || rule.getType() == ProjectBillingRuleType.HOURLY) {
            if (total.seconds() <= 0) {
                return;
            }
            lines.add(new BillingLine(total.projectKey(), total.projectId(), total.project(), total.projectName(),
                    month + " hourly work (" + durationDescription(total.seconds()) + ")",
                    total.seconds(), ONE, scaleMoney(total.rawAmount()), scaleMoney(total.rawAmount()), CURRENCY));
            return;
        }
        if (rule.getType() == ProjectBillingRuleType.FIXED_MONTHLY) {
            BigDecimal amount = scaleMoney(rule.getMonthlyAmount());
            lines.add(new BillingLine(total.projectKey(), total.projectId(), total.project(), total.projectName(),
                    month + " fixed monthly retainer", total.seconds(), ONE, amount, amount, CURRENCY));
            return;
        }

        BigDecimal baseAmount = scaleMoney(rule.getBaseAmount());
        BigDecimal includedHours = scaleHours(rule.getIncludedHours());
        BigDecimal overageRate = scaleMoney(rule.getOverageHourlyRate());
        BigDecimal workedHours = hours(total.seconds());
        BigDecimal overageHours = workedHours.subtract(includedHours);
        if (overageHours.compareTo(BigDecimal.ZERO) < 0) {
            overageHours = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        lines.add(new BillingLine(total.projectKey(), total.projectId(), total.project(), total.projectName(),
                month + " monthly retainer (" + includedHours.stripTrailingZeros().toPlainString()
                        + "h included, " + workedHours.stripTrailingZeros().toPlainString() + "h tracked)",
                Math.min(total.seconds(), includedHours.multiply(BigDecimal.valueOf(3600)).longValue()),
                ONE,
                baseAmount,
                baseAmount,
                CURRENCY));
        if (overageHours.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal overageAmount = overageHours.multiply(overageRate).setScale(2, RoundingMode.HALF_UP);
            lines.add(new BillingLine(total.projectKey(), total.projectId(), total.project(), total.projectName(),
                    month + " overage (" + overageHours.stripTrailingZeros().toPlainString() + "h over included hours)",
                    overageHours.multiply(BigDecimal.valueOf(3600)).longValue(),
                    overageHours,
                    overageRate,
                    overageAmount,
                    CURRENCY));
        }
    }

    private ProjectBillingRule activeRule(Project project, YearMonth month,
            Map<UUID, List<ProjectBillingRule>> providedRulesByProject) {
        if (project == null) {
            return null;
        }
        Map<UUID, List<ProjectBillingRule>> rulesByProject = providedRulesByProject == null
                ? rulesByProject(List.of(project))
                : providedRulesByProject;
        List<ProjectBillingRule> rules = rulesByProject.get(project.getId());
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        LocalDate monthStart = month.atDay(1);
        ProjectBillingRule active = null;
        for (ProjectBillingRule rule : rules) {
            if (!rule.getEffectiveFrom().isAfter(monthStart)) {
                active = rule;
            }
        }
        return active;
    }

    private BigDecimal monthAmount(ProjectBillingRule rule, BigDecimal rawAmount, long seconds) {
        if (rule == null || rule.getType() == ProjectBillingRuleType.HOURLY) {
            return scaleMoney(rawAmount);
        }
        if (rule.getType() == ProjectBillingRuleType.FIXED_MONTHLY) {
            return scaleMoney(rule.getMonthlyAmount());
        }
        BigDecimal overageHours = hours(seconds).subtract(scaleHours(rule.getIncludedHours()));
        if (overageHours.compareTo(BigDecimal.ZERO) < 0) {
            overageHours = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return scaleMoney(rule.getBaseAmount())
            .add(overageHours.multiply(scaleMoney(rule.getOverageHourlyRate())))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isRetainerRule(ProjectBillingRule rule) {
        return rule != null
            && (rule.getType() == ProjectBillingRuleType.FIXED_MONTHLY
                || rule.getType() == ProjectBillingRuleType.MONTHLY_BASE_PLUS_OVERAGE);
    }

    private Map<UUID, List<ProjectBillingRule>> rulesByProject(Collection<Project> projects) {
        List<UUID> ids = projects.stream()
            .map(Project::getId)
            .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return billingRuleRepository.findByProjectIdInOrderByProjectIdAscEffectiveFromAsc(ids).stream()
            .collect(Collectors.groupingBy(rule -> rule.getProject().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private List<YearMonth> fullMonths(LocalDate startDate, LocalDate endDate) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth last = YearMonth.from(endDate);
        while (!current.isAfter(last)) {
            if (!current.atDay(1).isBefore(startDate) && !current.atEndOfMonth().isAfter(endDate)) {
                months.add(current);
            }
            current = current.plusMonths(1);
        }
        return months;
    }

    private long overlapSeconds(Instant start, Instant end, Instant rangeStart, Instant rangeEnd) {
        Instant effectiveStart = start.isBefore(rangeStart) ? rangeStart : start;
        Instant effectiveEnd = end.isAfter(rangeEnd) ? rangeEnd : end;
        if (!effectiveEnd.isAfter(effectiveStart)) {
            return 0;
        }
        return Duration.between(effectiveStart, effectiveEnd).toSeconds();
    }

    private BigDecimal rawAmount(TimeEntry entry, long seconds) {
        return entry.getHourlyRate()
            .multiply(BigDecimal.valueOf(seconds))
            .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal hours(long seconds) {
        return BigDecimal.valueOf(seconds)
            .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleHours(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String projectKey(TimeEntry entry) {
        if (entry.getProject() != null) {
            return projectKey(entry.getProject());
        }
        return LEGACY_PROJECT_PREFIX + projectName(entry).trim().toLowerCase(Locale.ROOT);
    }

    private String projectKey(Project project) {
        return project.getId().toString();
    }

    private String projectName(TimeEntry entry) {
        return entry.getProject() == null ? entry.getProjectName() : entry.getProject().getName();
    }

    private MonthWindow monthWindow(YearMonth month, ZoneId zone) {
        return new MonthWindow(month.atDay(1).atStartOfDay(zone).toInstant(),
                month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant());
    }

    private Comparator<ProjectAccumulator> projectComparator() {
        return Comparator.comparingLong(ProjectAccumulator::seconds).reversed()
            .thenComparing(ProjectAccumulator::projectName, String.CASE_INSENSITIVE_ORDER);
    }

    private String durationDescription(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours == 0) {
            return minutes + "m";
        }
        return hours + "h" + String.format("%02d", minutes) + "m";
    }

    private record Calculation(
            Map<String, ProjectAccumulator> projectTotals,
            Map<String, ProjectAccumulator> partialTotals,
            Map<ProjectMonthKey, ProjectMonthTotals> monthTotals
    ) {
    }

    private record MonthWindow(Instant start, Instant end) {
    }

    private record ProjectMonthKey(String projectKey, YearMonth month) {
    }

    private static final class ProjectMonthTotals {
        private final String projectKey;
        private final Project project;
        private final String projectName;
        private final YearMonth month;
        private long seconds;
        private BigDecimal rawAmount = ZERO;
        private BigDecimal billingAmount = ZERO;

        private ProjectMonthTotals(String projectKey, Project project, String projectName, YearMonth month) {
            this.projectKey = projectKey;
            this.project = project;
            this.projectName = projectName;
            this.month = month;
        }

        private void add(long seconds, BigDecimal rawAmount) {
            this.seconds += seconds;
            this.rawAmount = this.rawAmount.add(rawAmount).setScale(2, RoundingMode.HALF_UP);
        }

        private String projectKey() {
            return projectKey;
        }

        private UUID projectId() {
            return project == null ? null : project.getId();
        }

        private Project project() {
            return project;
        }

        private String projectName() {
            return projectName;
        }

        private YearMonth month() {
            return month;
        }

        private long seconds() {
            return seconds;
        }

        private BigDecimal rawAmount() {
            return rawAmount;
        }

        private void setBillingAmount(BigDecimal billingAmount) {
            this.billingAmount = billingAmount;
        }

        @SuppressWarnings("unused")
        private BigDecimal billingAmount() {
            return billingAmount;
        }
    }
}
