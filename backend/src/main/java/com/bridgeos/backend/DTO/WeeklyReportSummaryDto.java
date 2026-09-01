package com.bridgeos.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportSummaryDto {
    private int tasksResolved;
    private int tasksCreated;
    private double avgClarityScore;
    private double completionRate;
    private double avgResolutionHours;
    private int rankInTeam;
    private int totalTeamResolved;
    private double teamAvgClarity;
}
