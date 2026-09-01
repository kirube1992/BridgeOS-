package com.bridgeos.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamWeeklyReportEntryDto {
    private WeeklyReportUserDto user;
    private WeeklyReportSummaryDto summary;
}
