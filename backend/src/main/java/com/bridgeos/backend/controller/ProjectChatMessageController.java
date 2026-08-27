package com.bridgeos.backend.controller;

import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.entity.ProjectChatMessage;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.repository.ProjectChatMessageRepository;
import com.bridgeos.backend.service.ProjectService;
import com.bridgeos.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/projects/{projectId}/messages")
@RequiredArgsConstructor
@Slf4j
public class ProjectChatMessageController {

    private final ProjectChatMessageRepository chatRepository;
    private final ProjectService projectService;
    private final UserService userService;

    // ConcurrentMap of registered SseEmitters per Project ID
    private static final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // GET /api/projects/{projectId}/messages
    @GetMapping
    public ResponseEntity<List<ProjectChatMessage>> getChatHistory(@PathVariable Long projectId) {
        log.info("GET /api/projects/{}/messages - Fetching chat history", projectId);
        // Verify project exists
        projectService.getProjectById(projectId);
        List<ProjectChatMessage> history = chatRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
        return ResponseEntity.ok(history);
    }

    // POST /api/projects/{projectId}/messages
    @PostMapping
    public ResponseEntity<ProjectChatMessage> sendMessage(
            @PathVariable Long projectId,
            @RequestBody MessageRequest request,
            Principal principal) {
        log.info("POST /api/projects/{}/messages - Sending new message", projectId);

        Project project = projectService.getProjectById(projectId);
        User sender = userService.getUserByEmail(principal.getName());

        ProjectChatMessage message = new ProjectChatMessage();
        message.setProject(project);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setCreatedAt(LocalDateTime.now());

        ProjectChatMessage saved = chatRepository.save(message);

        // Broadcast to all emitters listening to this project
        broadcastMessage(projectId, saved);

        return ResponseEntity.ok(saved);
    }

    // GET /api/projects/{projectId}/messages/stream
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessages(@PathVariable Long projectId) {
        log.info("GET /api/projects/{}/messages/stream - Creating new SSE connection", projectId);

        // 30 minutes timeout
        SseEmitter emitter = new SseEmitter(1800000L);

        // Initialize emitter list for project if not present
        emitters.computeIfAbsent(projectId, k -> new ArrayList<>());
        emitters.get(projectId).add(emitter);

        // Send dummy connection established event to avoid browser connection timeout issues
        try {
            emitter.send(SseEmitter.event().name("init").data("Connected to Project " + projectId));
        } catch (IOException e) {
            log.error("Failed to send init event to emitter", e);
            emitter.completeWithError(e);
        }

        emitter.onCompletion(() -> removeEmitter(projectId, emitter));
        emitter.onTimeout(() -> removeEmitter(projectId, emitter));
        emitter.onError((e) -> removeEmitter(projectId, emitter));

        return emitter;
    }

    private void removeEmitter(Long projectId, SseEmitter emitter) {
        List<SseEmitter> projectEmitters = emitters.get(projectId);
        if (projectEmitters != null) {
            projectEmitters.remove(emitter);
            if (projectEmitters.isEmpty()) {
                emitters.remove(projectId);
            }
        }
    }

    private void broadcastMessage(Long projectId, ProjectChatMessage message) {
        List<SseEmitter> projectEmitters = emitters.get(projectId);
        if (projectEmitters == null || projectEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : projectEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(message));
            } catch (Exception e) {
                log.error("Error sending message to SseEmitter", e);
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            projectEmitters.removeAll(deadEmitters);
            if (projectEmitters.isEmpty()) {
                emitters.remove(projectId);
            }
        }
    }

    public static class MessageRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
