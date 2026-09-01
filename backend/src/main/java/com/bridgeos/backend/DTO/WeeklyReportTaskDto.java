package com.bridgeos.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportTaskDto {
    private Long id;
    private String title;
    private String status;
    private LocalDateTime completedAt;
}
