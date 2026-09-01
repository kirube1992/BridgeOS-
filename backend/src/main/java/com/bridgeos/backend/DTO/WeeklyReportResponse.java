package com.bridgeos.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportResponse {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private WeeklyReportUserDto user;
    private WeeklyReportSummaryDto summary;
    private WeeklyReportTrendsDto trends;
    private List<WeeklyReportTaskDto> recentTasks;
    private List<WeeklyReportTaskDto> createdTasks;
    private List<WeeklyReportDecisionDto> recentDecisions;
    private List<LocalDate> availableWeeks;
    private boolean emailOptIn;
}
