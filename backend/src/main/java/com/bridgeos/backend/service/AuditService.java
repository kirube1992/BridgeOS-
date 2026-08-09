package com.bridgeos.backend.service;


import com.bridgeos.backend.entity.AuditEvent;
import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.repository.AuditEventRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    public List<AuditEvent> search(String keyword, Long projectId, String from, String to) {
        LocalDateTime fromDate = parseDate(from);
        LocalDateTime toDate = parseDate(to);

        if (projectId != null) {
            return auditEventRepository.findByProjectIdAndAtBetween(projectId, fromDate, toDate);
        }

        if (keyword != null && !keyword.isEmpty()) {
            return auditEventRepository.searchByKeyword(keyword);
        }

        return auditEventRepository.findByCreatedAtBetween(fromDate, toDate);
    }

    public List<AuditEvent> getProjectTimeline(Long projectId) {
        return auditEventRepository.findByActorId(projectId);
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
