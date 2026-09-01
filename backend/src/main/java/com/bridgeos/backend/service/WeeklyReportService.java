package com.bridgeos.backend.service;

import com.bridgeos.backend.DTO.*;
import com.bridgeos.backend.entity.AuditEvent;
import com.bridgeos.backend.entity.MetricsDaily;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.entity.WorkItemStatus;
import com.bridgeos.backend.repository.AuditEventRepository;
import com.bridgeos.backend.repository.MetricsDailyRepository;
import com.bridgeos.backend.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportService {

    private final UserService userService;
    private final WorkItemRepository workItemRepository;
    private final AuditEventRepository auditEventRepository;
    private final MetricsDailyRepository metricsDailyRepository;

    public WeeklyReportResponse getCurrentUserReport(User requester, LocalDate weekStart) {
        return buildReport(requester, requester.getId(), resolveWeekStart(weekStart));
    }

    public WeeklyReportResponse getUserReport(User requester, Long userId, LocalDate weekStart) {
        assertCanViewUserReport(requester, userId);
        return buildReport(requester, userId, resolveWeekStart(weekStart));
    }

    public TeamWeeklyReportResponse getTeamReport(LocalDate weekStart) {
        LocalDate start = resolveWeekStart(weekStart);
        LocalDate end = start.plusDays(6);
        List<User> users = userService.getAlluser();

        List<TeamWeeklyReportEntryDto> members = users.stream()
                .map(user -> new TeamWeeklyReportEntryDto(
                        toUserDto(user),
                        buildSummary(user, start, end, users)
                ))
                .sorted(Comparator.comparingInt((TeamWeeklyReportEntryDto entry) -> entry.getSummary().getTasksResolved()).reversed())
                .toList();

        int totalTeamResolved = members.stream()
                .mapToInt(entry -> entry.getSummary().getTasksResolved())
                .sum();

        double teamAvgClarity = members.stream()
                .mapToDouble(entry -> entry.getSummary().getAvgClarityScore())
                .average()
                .orElse(0.0);

        return new TeamWeeklyReportResponse(start, end, totalTeamResolved, round(teamAvgClarity), members);
    }

    public void updateEmailPreference(User user, boolean emailOptIn) {
        user.setWeeklyReportEmailOptIn(emailOptIn);
        userService.saveUser(user);
    }

    private WeeklyReportResponse buildReport(User requester, Long userId, LocalDate weekStart) {
        User user = userService.getUserById(userId);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<User> allUsers = userService.getAlluser();

        WeeklyReportSummaryDto summary = buildSummary(user, weekStart, weekEnd, allUsers);
        WeeklyReportTrendsDto trends = buildTrends(user, weekStart, weekEnd);
        List<WeeklyReportTaskDto> recentTasks = getResolvedTasks(user, weekStart, weekEnd);
        List<WeeklyReportTaskDto> createdTasks = getCreatedTasks(user, weekStart, weekEnd);
        List<WeeklyReportDecisionDto> recentDecisions = getDecisions(user, weekStart, weekEnd);

        return new WeeklyReportResponse(
                weekStart,
                weekEnd,
                toUserDto(user),
                summary,
                trends,
                recentTasks,
                createdTasks,
                recentDecisions,
                buildAvailableWeeks(),
                requester.getId().equals(userId) && user.isWeeklyReportEmailOptIn()
        );
    }

    private WeeklyReportSummaryDto buildSummary(User user, LocalDate weekStart, LocalDate weekEnd, List<User> allUsers) {
        List<WorkItem> resolvedItems = getResolvedWorkItems(user, weekStart, weekEnd);
        List<WorkItem> createdItems = getCreatedWorkItems(user, weekStart, weekEnd);

        int tasksResolved = resolvedItems.size();
        int tasksCreated = createdItems.size();

        double avgClarity = resolvedItems.stream()
                .mapToInt(item -> item.getClarityScore() != null ? item.getClarityScore() : 0)
                .average()
                .orElse(0.0);

        if (avgClarity == 0.0) {
            avgClarity = metricsDailyRepository
                    .findByUserIdAndMetricDateBetween(user.getId(), weekStart, weekEnd)
                    .stream()
                    .map(MetricsDaily::getClarityAvgScore)
                    .filter(Objects::nonNull)
                    .mapToDouble(score -> score.doubleValue())
                    .average()
                    .orElse(0.0);
        }

        double avgResolutionHours = resolvedItems.stream()
                .mapToLong(item -> ChronoUnit.HOURS.between(item.getCreatedAt(), item.getUpdatedAt()))
                .average()
                .orElse(0.0);

        int openItems = getAssignedOpenItems(user, weekStart, weekEnd).size();
        int denominator = Math.max(tasksResolved + openItems, 1);
        double completionRate = round((tasksResolved * 100.0) / denominator);

        Map<Long, Integer> resolvedCounts = new HashMap<>();
        for (User teamMember : allUsers) {
            resolvedCounts.put(
                    teamMember.getId(),
                    getResolvedWorkItems(teamMember, weekStart, weekEnd).size()
            );
        }

        int totalTeamResolved = resolvedCounts.values().stream().mapToInt(Integer::intValue).sum();
        int rank = calculateRank(user.getId(), resolvedCounts);

        double teamAvgClarity = allUsers.stream()
                .mapToDouble(member -> {
                    List<WorkItem> memberResolved = getResolvedWorkItems(member, weekStart, weekEnd);
                    return memberResolved.stream()
                            .mapToInt(item -> item.getClarityScore() != null ? item.getClarityScore() : 0)
                            .average()
                            .orElse(0.0);
                })
                .filter(score -> score > 0)
                .average()
                .orElse(0.0);

        return new WeeklyReportSummaryDto(
                tasksResolved,
                tasksCreated,
                round(avgClarity),
                completionRate,
                round(avgResolutionHours),
                rank,
                totalTeamResolved,
                round(teamAvgClarity)
        );
    }

    private WeeklyReportTrendsDto buildTrends(User user, LocalDate weekStart, LocalDate weekEnd) {
        List<Integer> clarityScores = new ArrayList<>();
        List<Integer> resolvedByDay = new ArrayList<>();
        List<Integer> createdByDay = new ArrayList<>();

        for (int offset = 0; offset < 7; offset++) {
            LocalDate day = weekStart.plusDays(offset);
            List<WorkItem> resolvedForDay = getResolvedWorkItems(user, day, day);
            List<WorkItem> createdForDay = getCreatedWorkItems(user, day, day);

            resolvedByDay.add(resolvedForDay.size());
            createdByDay.add(createdForDay.size());

            int dailyClarity = (int) Math.round(resolvedForDay.stream()
                    .mapToInt(item -> item.getClarityScore() != null ? item.getClarityScore() : 0)
                    .average()
                    .orElse(0.0));
            clarityScores.add(dailyClarity);
        }

        return new WeeklyReportTrendsDto(clarityScores, resolvedByDay, createdByDay);
    }

    private List<WeeklyReportTaskDto> getResolvedTasks(User user, LocalDate weekStart, LocalDate weekEnd) {
        return getResolvedWorkItems(user, weekStart, weekEnd).stream()
                .sorted(Comparator.comparing(WorkItem::getUpdatedAt).reversed())
                .map(item -> new WeeklyReportTaskDto(
                        item.getId(),
                        item.getTitle(),
                        item.getStatus().name(),
                        item.getUpdatedAt()
                ))
                .toList();
    }

    private List<WeeklyReportTaskDto> getCreatedTasks(User user, LocalDate weekStart, LocalDate weekEnd) {
        return getCreatedWorkItems(user, weekStart, weekEnd).stream()
                .sorted(Comparator.comparing(WorkItem::getCreatedAt).reversed())
                .map(item -> new WeeklyReportTaskDto(
                        item.getId(),
                        item.getTitle(),
                        item.getStatus().name(),
                        item.getCreatedAt()
                ))
                .toList();
    }

    private List<WeeklyReportDecisionDto> getDecisions(User user, LocalDate weekStart, LocalDate weekEnd) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekEnd.atTime(LocalTime.MAX);

        return auditEventRepository.findByActorIdAndCreatedAtBetween(user.getId(), start, end).stream()
                .filter(event -> "DECISION".equalsIgnoreCase(event.getEntityType())
                        || "DECISION_RECORDED".equalsIgnoreCase(event.getEventType()))
                .sorted(Comparator.comparing(AuditEvent::getCreatedAt).reversed())
                .map(event -> new WeeklyReportDecisionDto(
                        event.getId(),
                        event.getSummery(),
                        event.getCreatedAt()
                ))
                .toList();
    }

    private List<WorkItem> getResolvedWorkItems(User user, LocalDate weekStart, LocalDate weekEnd) {
        return workItemRepository.findByAssignedToId(user.getId()).stream()
                .filter(item -> item.getStatus() == WorkItemStatus.DONE)
                .filter(item -> item.getUpdatedAt() != null)
                .filter(item -> !item.getUpdatedAt().toLocalDate().isBefore(weekStart))
                .filter(item -> !item.getUpdatedAt().toLocalDate().isAfter(weekEnd))
                .collect(Collectors.toList());
    }

    private List<WorkItem> getCreatedWorkItems(User user, LocalDate weekStart, LocalDate weekEnd) {
        return workItemRepository.findAll().stream()
                .filter(item -> item.getCreatedBy() != null && item.getCreatedBy().getId().equals(user.getId()))
                .filter(item -> item.getCreatedAt() != null)
                .filter(item -> !item.getCreatedAt().toLocalDate().isBefore(weekStart))
                .filter(item -> !item.getCreatedAt().toLocalDate().isAfter(weekEnd))
                .collect(Collectors.toList());
    }

    private List<WorkItem> getAssignedOpenItems(User user, LocalDate weekStart, LocalDate weekEnd) {
        return workItemRepository.findByAssignedToId(user.getId()).stream()
                .filter(item -> item.getStatus() != WorkItemStatus.DONE)
                .filter(item -> item.getUpdatedAt() != null || item.getCreatedAt() != null)
                .filter(item -> {
                    LocalDate referenceDate = item.getUpdatedAt() != null
                            ? item.getUpdatedAt().toLocalDate()
                            : item.getCreatedAt().toLocalDate();
                    return !referenceDate.isBefore(weekStart) && !referenceDate.isAfter(weekEnd);
                })
                .collect(Collectors.toList());
    }

    private int calculateRank(Long userId, Map<Long, Integer> resolvedCounts) {
        List<Map.Entry<Long, Integer>> sorted = resolvedCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .toList();

        for (int index = 0; index < sorted.size(); index++) {
            if (sorted.get(index).getKey().equals(userId)) {
                return index + 1;
            }
        }
        return sorted.size();
    }

    private List<LocalDate> buildAvailableWeeks() {
        List<LocalDate> weeks = new ArrayList<>();
        LocalDate current = resolveWeekStart(null);
        for (int i = 0; i < 12; i++) {
            weeks.add(current.minusWeeks(i));
        }
        return weeks;
    }

    private LocalDate resolveWeekStart(LocalDate weekStart) {
        if (weekStart != null) {
            return weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private WeeklyReportUserDto toUserDto(User user) {
        return new WeeklyReportUserDto(user.getId(), user.getName(), user.getEmail());
    }

    private void assertCanViewUserReport(User requester, Long userId) {
        if (!requester.getId().equals(userId) && !"ADMIN".equalsIgnoreCase(requester.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own weekly report");
        }
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
