package com.bridgeos.backend.controller;


import com.bridgeos.backend.entity.AuditEvent;
import com.bridgeos.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/audit")
public class AuditController {

    private  final AuditService auditService;

    @GetMapping("/search")
    public ResponseEntity<List<AuditEvent>> searchAudit(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        log.info("GET /api/audit/search - q={}, projectId", q, projectId);
        List<AuditEvent> results = auditService.search(q, projectId, from,to);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AuditEvent>> getProjectTimeline(@PathVariable Long projectId){
        log.info("GET/api/audit/project/{} Getting timeline", projectId);
        List<AuditEvent> events = auditService.getProjectTimeline(projectId);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/decisions")
    public ResponseEntity<AuditEvent> recordDecision(@Valid @RequestBody DecisionRequest request,
                                                     java.security.Principal principal) {
        log.info("POST /api/audit/decisions - Recording decision");

        Long actorId = auditService.getUserIdByEmail(principal.getName());

        AuditEvent event =  auditService.recordDecision(
                actorId,
                request.getProjectId(),
                request.getDecision(),
                request.getContext()
        );
        return  ResponseEntity.status(HttpStatus.CREATED).body(event);

    }

    @PutMapping("/decisions/{id}")
    public ResponseEntity<AuditEvent> updateDecision(@PathVariable Long id,
                                                     @Valid @RequestBody DecisionRequest request) {
        AuditEvent event = auditService.updateDecision(
                id,
                request.getProjectId(),
                request.getDecision(),
                request.getContext()
        );
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/decisions/{id}")
    public ResponseEntity<Void> deleteDecision(@PathVariable Long id) {
        auditService.deleteDecision(id);
        return ResponseEntity.noContent().build();
    }

    static class DecisionRequest {
        @NotBlank
        private String decision;
        private String context;
        @NotNull
        private Long projectId;

        // Getters and setters
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
    }
}
