package com.bridgeos.backend.controller;

import com.bridgeos.backend.DTO.TeamWeeklyReportResponse;
import com.bridgeos.backend.DTO.WeeklyReportResponse;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.service.UserService;
import com.bridgeos.backend.service.WeeklyReportEmailService;
import com.bridgeos.backend.service.WeeklyReportService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports/weekly")
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;
    private final WeeklyReportEmailService weeklyReportEmailService;
    private final UserService userService;

    @GetMapping("/current")
    public ResponseEntity<WeeklyReportResponse> getCurrentReport(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        User user = userService.getUserByEmail(principal.getUsername());
        return ResponseEntity.ok(weeklyReportService.getCurrentUserReport(user, weekStart));
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamWeeklyReportResponse> getTeamReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(weeklyReportService.getTeamReport(weekStart));
    }

    @GetMapping("/{userId}/{weekStart}")
    public ResponseEntity<WeeklyReportResponse> getReportForWeek(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        User requester = userService.getUserByEmail(principal.getUsername());
        return ResponseEntity.ok(weeklyReportService.getUserReport(requester, userId, weekStart));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WeeklyReportResponse> getUserReport(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        User requester = userService.getUserByEmail(principal.getUsername());
        return ResponseEntity.ok(weeklyReportService.getUserReport(requester, userId, weekStart));
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendReportEmail(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getUserByEmail(principal.getUsername());
        weeklyReportEmailService.sendReportEmail(user);
        return ResponseEntity.ok(Map.of("message", "Weekly report email queued successfully"));
    }

    @PutMapping("/preferences")
    public ResponseEntity<Map<String, Object>> updatePreferences(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody ReportPreferencesRequest request) {
        User user = userService.getUserByEmail(principal.getUsername());
        weeklyReportService.updateEmailPreference(user, request.isEmailOptIn());
        return ResponseEntity.ok(Map.of(
                "emailOptIn", user.isWeeklyReportEmailOptIn(),
                "message", "Report preferences updated"
        ));
    }

    @Data
    public static class ReportPreferencesRequest {
        private boolean emailOptIn;
    }
}
