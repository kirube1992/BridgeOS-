package com.bridgeos.backend.service;


import com.bridgeos.backend.entity.AuditEvent;
import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.repository.AuditEventRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final UserService userService;
    private  final ProjectService projectService;
    private final AuditEventRepository auditEventRepository;


    public AuditEvent recordEvent(String eventType, String entityType, Long entityID, String summary, Object detail, Long actorID, Long projectId){
        AuditEvent event = new AuditEvent();

        event.setEventType(eventType);
        event.setEntityType(entityType);
        event.setEntityId(entityID);
        event.setSummery(summary);
        event.setDetail(detail);

        if(actorID !=  null) {
            User actor = userService.getUserById(actorID);
            event.setActor(actor);
        }

        if(projectId != null) {
            Project project = projectService.getProjectById(projectId);
            event.setProject(project);
        }

        event.setCreatedAt(LocalDateTime.now());

        log.info("Audit event recorded: {} - {}", eventType, summary);

        return  auditEventRepository.save(event);

    }

    public AuditEvent recordDecision(Long userId, Long projectId, String decision, String context) {
        return  recordEvent(
                "DECISION_RECORDED",
                "DECISION",
                null,
                decision,
                context,
                userId,
                projectId
        );

    }

    public AuditEvent updateDecision(Long id, Long projectId, String decision, String context) {
        AuditEvent event = findDecision(id);
        event.setSummery(decision);
        event.setDetail(context);
        event.setProject(projectService.getProjectById(projectId));
        return auditEventRepository.save(event);
    }

    public void deleteDecision(Long id) {
        AuditEvent event = findDecision(id);
        auditEventRepository.delete(event);
    }

    private AuditEvent findDecision(Long id) {
        AuditEvent event = auditEventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Decision not found"));
        if (!"DECISION".equals(event.getEntityType())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Decision not found");
        }
        return event;
    }

    public Long getUserIdByEmail(String email) {
        return userService.getUserByEmail(email).getId();
    }
    public List<AuditEvent> search(String keyword, Long projectId, String from, String to) {
        LocalDateTime fromDate = parseDate(from);
        LocalDateTime toDate = parseDate(to);

        if (projectId != null) {
            return auditEventRepository.findByProjectIdAndCreatedAtBetween(projectId, fromDate, toDate);
        }

        if (keyword != null && !keyword.isEmpty()) {
            return auditEventRepository.searchByKeyword(keyword);
        }

        if (from == null && to == null) {
            return auditEventRepository.findAllByOrderByCreatedAtDesc();
        }

        return auditEventRepository.findByCreatedAtBetween(fromDate, toDate);
    }

    public List<AuditEvent> getProjectTimeline(Long projectId) {
        return auditEventRepository.findByProjectId(projectId);
    }

    private LocalDateTime parseDate(String dateString) {
        if (dateString == null) {
            return LocalDateTime.now().minusYears(1);
        }
        try {
            return LocalDateTime.parse(dateString + "T00:00:00");
        } catch (Exception e) {
            return LocalDateTime.now().minusYears(1);
        }
    }



}
