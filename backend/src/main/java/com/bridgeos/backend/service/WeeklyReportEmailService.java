package com.bridgeos.backend.service;

import com.bridgeos.backend.DTO.TeamWeeklyReportResponse;
import com.bridgeos.backend.DTO.WeeklyReportResponse;
import com.bridgeos.backend.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportEmailService {

    private final WeeklyReportService weeklyReportService;
    private final UserService userService;
    private final JavaMailSender mailSender;

    @Value("${app.reports.base-url:http://localhost:5217}")
    private String reportsBaseUrl;

    @Value("${app.reports.email.enabled:false}")
    private boolean emailEnabled;

    @Scheduled(cron = "0 0 23 * * SUN")
    public void sendWeeklyReportEmails() {
        log.info("Starting scheduled weekly report email delivery");
        List<User> users = userService.getAlluser().stream()
                .filter(User::isWeeklyReportEmailOptIn)
                .toList();

        for (User user : users) {
            try {
                sendReportEmail(user);
            } catch (Exception ex) {
                log.error("Failed to send weekly report email to {}", user.getEmail(), ex);
            }
        }
    }

    public void sendReportEmail(User user) {
        WeeklyReportResponse report = weeklyReportService.getCurrentUserReport(user, null);
        String subject = String.format(
                "BridgeOS Weekly Report: %s - %s",
                formatDate(report.getWeekStart()),
                formatDate(report.getWeekEnd())
        );
        String html = buildEmailHtml(report);

        if (!emailEnabled) {
            log.info("Weekly report email disabled. Would send to {}:\nSubject: {}\n{}", user.getEmail(), subject, html);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Weekly report email sent to {}", user.getEmail());
        } catch (Exception ex) {
            log.error("Failed to send weekly report email to {}", user.getEmail(), ex);
            throw new RuntimeException("Failed to send weekly report email", ex);
        }
    }

    private String buildEmailHtml(WeeklyReportResponse report) {
        return """
            <html>
              <body style="font-family: Arial, sans-serif; background:#f8fafc; padding:24px;">
                <div style="max-width:560px;margin:0 auto;background:#ffffff;border:1px solid #e5e7eb;border-radius:12px;padding:24px;">
                  <h1 style="margin:0 0 8px;color:#10232b;">Your Weekly Report</h1>
                  <p style="color:#64748b;">%s - %s</p>
                  <table style="width:100%%;margin-top:20px;border-collapse:collapse;">
                    <tr><td style="padding:8px 0;">Tasks resolved</td><td style="text-align:right;font-weight:bold;">%d</td></tr>
                    <tr><td style="padding:8px 0;">Tasks created</td><td style="text-align:right;font-weight:bold;">%d</td></tr>
                    <tr><td style="padding:8px 0;">Avg. clarity</td><td style="text-align:right;font-weight:bold;">%.0f</td></tr>
                    <tr><td style="padding:8px 0;">Completion rate</td><td style="text-align:right;font-weight:bold;">%.1f%%</td></tr>
                    <tr><td style="padding:8px 0;">Team rank</td><td style="text-align:right;font-weight:bold;">#%d</td></tr>
                  </table>
                  <a href="%s/reports" style="display:inline-block;margin-top:24px;background:#245360;color:#ffffff;text-decoration:none;padding:12px 18px;border-radius:8px;font-weight:bold;">View Full Report</a>
                </div>
              </body>
            </html>
            """.formatted(
                formatDate(report.getWeekStart()),
                formatDate(report.getWeekEnd()),
                report.getSummary().getTasksResolved(),
                report.getSummary().getTasksCreated(),
                report.getSummary().getAvgClarityScore(),
                report.getSummary().getCompletionRate(),
                report.getSummary().getRankInTeam(),
                reportsBaseUrl
        );
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }
}
