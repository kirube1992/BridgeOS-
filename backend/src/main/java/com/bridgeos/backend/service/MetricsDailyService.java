package com.bridgeos.backend.service;


import com.bridgeos.backend.entity.MetricsDaily;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.entity.WorkItemStatus;
import com.bridgeos.backend.repository.MetricsDailyRepository;
import com.bridgeos.backend.repository.WorkItemRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Data
public class MetricsDailyService {

    private final MetricsDailyRepository metricsDailyRepository;
    private final UserService userService;
    private final WorkItemRepository workItemRepository;


    public void triggerManualMetricsGeneration() {
        log.info("Manual trigger - generating daily metrics");
        generateDailyMetric();
    }



    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void generateDailyMetric() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Generating  daily metrics for: {}", yesterday);

        List<User>  users = userService.getAlluser();

        for (User user:users){
            generateMetricsForUser(yesterday, user);
        }

        log.info("Daily metrics generation completed for: {}", yesterday);
    }

    private void generateMetricsForUser(LocalDate date, User user) {
        List<WorkItem> resolvedItems = getResolvedItemsForUser(date, user);
        List<WorkItem> createdItems = getCreatedItemsForUser(date, user);
        List<WorkItem> allIItems = getItemsForUser(date, user);


        MetricsDaily metrics = new MetricsDaily();
        metrics.setMetricDate(date);
        metrics.setUser(user);
        metrics.setDepartment(user.getDepartment());

        metrics.setItemsResolved(resolvedItems.size());

        metrics.setItemsCreated(createdItems.size());


        double avgHours = calculateAverageResolutionHours(resolvedItems);
        metrics.setAvgResolutionHours(BigDecimal.valueOf(avgHours).setScale(2, RoundingMode.HALF_UP));


        double medianHours  = calculateMedianResolutionHours(resolvedItems);
        metrics.setMedianResolutionHours(BigDecimal.valueOf(medianHours).setScale(2, RoundingMode.HALF_UP));

        metrics.setCsatAvg(BigDecimal.ZERO);
        metrics.setKudosReceived(0);

        metrics.setOffHoursResolutions(countOffHoursResolutions(resolvedItems,user));
        metricsDailyRepository.save(metrics);
    }

    private List<WorkItem>  getResolvedItemsForUser(LocalDate date, User user) {
        return workItemRepository.findByAssignedToId(user.getId()).stream()
                .filter(item -> item.getStatus()== WorkItemStatus.DONE)
                .filter(item -> item.getUpdatedAt() != null)
                .filter(item -> item.getUpdatedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    private List<WorkItem> getCreatedItemsForUser(LocalDate date, User user) {
        return  workItemRepository.findAll().stream()
                .filter(items -> items.getCreatedBy().getId().equals(user.getId()))
                .filter(items -> items.getCreatedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    private List<WorkItem> getItemsForUser(LocalDate date, User user) {
        return workItemRepository.findByAssignedToId(user.getId()).stream()
                .filter(item -> item.getUpdatedAt() != null)
                .filter(item -> item.getUpdatedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    private double calculateAverageResolutionHours(List<WorkItem> resolvedItems) {
        if(resolvedItems.isEmpty()) return 0.0;
        long totalHours = resolvedItems.stream()
                .mapToLong(items -> ChronoUnit.HOURS.between(items.getCreatedAt(), items.getUpdatedAt()))
                .sum();
        return  (double)  totalHours / resolvedItems.size();
    }

    private double calculateMedianResolutionHours(List<WorkItem> resolvedItems) {
        if (resolvedItems.isEmpty()) return 0.0;
        List<Long> hours = resolvedItems.stream()
                .map(item -> ChronoUnit.HOURS.between(item.getCreatedAt(), item.getUpdatedAt()))
                .sorted()
                .collect(Collectors.toList());
        int size = hours.size();
        if (size % 2 == 0) {
            return (hours.get(size / 2 - 1) + hours.get(size / 2)) / 2.0;
        } else {
            return hours.get(size / 2);
        }
    }
    private double calculateAverageClarityScore(List<WorkItem> items) {
        if (items.isEmpty()) return 0.0;
        double avg = items.stream()
                .mapToInt(item -> item.getClarityScore() != null ? item.getClarityScore() : 0)
                .average()
                .orElse(0.0);
        return avg;
    }
    private int countOffHoursResolutions(List<WorkItem> resolvedItems, User user) {
        return 0;
    }

}
