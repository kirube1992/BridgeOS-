package com.bridgeos.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportTrendsDto {
    private List<Integer> clarityScores;
    private List<Integer> resolvedByDay;
    private List<Integer> createdByDay;
}
