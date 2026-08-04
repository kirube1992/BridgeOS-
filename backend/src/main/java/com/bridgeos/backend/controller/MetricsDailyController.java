package com.bridgeos.backend.controller;


import com.bridgeos.backend.entity.MetricsDaily;
import com.bridgeos.backend.repository.MetricsDailyRepository;
import com.bridgeos.backend.service.MetricsDailyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class MetricsDailyController {

    private final MetricsDailyRepository metricsDailyRepository;
    private final MetricsDailyService metricsDailyService;


    @PostMapping("/metrics/trigger")
    public ResponseEntity<String> triggerMetrics() {
        log.info("Manually triggering metrics generation");
        metricsDailyService.triggerManualMetricsGeneration();
        return ResponseEntity.ok("Metrics generation triggered successfully!");
    }

    @GetMapping("/metrics/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Metrics controller is alive!");
    }

    @GetMapping("/leaderbord")
    public ResponseEntity<List<MetricsDaily>> getLeaderboard(@RequestParam(defaultValue = "week") String period, @RequestParam(required = false) Long departmentId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate =   getStartDateForPeriod(period);

        List<MetricsDaily> metrics;

        if(departmentId != null) {
            metrics = metricsDailyRepository.findByMetricDateBetweenAndDepartmentId(startDate,endDate,departmentId);
        } else {
            metrics = metricsDailyRepository.findByMetricDateBetween(startDate, endDate);
        }

        metrics.sort((a,b)->b.getItemsResolved().compareTo(a.getItemsResolved()));

        return  ResponseEntity.ok(metrics);
    }



    @GetMapping("/user/{UserId}")
    public ResponseEntity<List<MetricsDaily>> getUserMetrics(@PathVariable Long UerId) {
        List<MetricsDaily> metrics = metricsDailyRepository.findByUserIdOrderByMetricDateDesc(UerId);

        return  ResponseEntity.ok(metrics);
    }


    public ResponseEntity<SummaryDto> getSummary() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        List<MetricsDaily> metrics = metricsDailyRepository.findByMetricDateBetween(startDate, LocalDate.now());

        int totalResolved = metrics.stream().mapToInt(MetricsDaily::getItemsResolved).sum();
        double avgResolution = metrics.stream()
                .mapToDouble(m -> m.getAvgResolutionHours().doubleValue())
                .average()
                .orElse(0.0);

        return ResponseEntity.ok(new SummaryDto(totalResolved, avgResolution));
    }

    private LocalDate getStartDateForPeriod(String period) {
        switch (period.toLowerCase()) {
            case "week":
                return LocalDate.now().minusDays(7);
            case "month":
                return LocalDate.now().minusDays(30);
            case "quarter":
                return LocalDate.now().minusDays(90);
            default:
                return LocalDate.now().minusDays(7);
        }
    }

    record SummaryDto(int totalResolved, double avgResolutionHours) {}


}
