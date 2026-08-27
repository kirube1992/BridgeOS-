package com.bridgeos.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectAnalyticsDto {
    private Long projectId;
    private String projectName;
    private long totalTasks;
    private long completed;
    private long inProgress;
    private long review;
    private long todo;
    private double avgClarity;
    private double resolutionDays;
}
