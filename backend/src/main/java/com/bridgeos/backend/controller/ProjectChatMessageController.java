package com.bridgeos.backend.controller;

import com.bridgeos.backend.DTO.ChatMessageResponse;
import com.bridgeos.backend.DTO.PagedChatResponse;
import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.entity.ProjectChatMessage;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.repository.ProjectChatMessageRepository;
import com.bridgeos.backend.service.ProjectService;
import com.bridgeos.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/{projectId}/messages")
@RequiredArgsConstructor
@Slf4j
public class ProjectChatMessageController {

    private final ProjectChatMessageRepository chatRepository;
    private final ProjectService projectService;
    private final UserService userService;

    private static final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @GetMapping
    public ResponseEntity<PagedChatResponse> getChatHistory(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("GET /api/projects/{}/messages - page={}, size={}", projectId, page, size);
        projectService.getProjectById(projectId);

        Page<ProjectChatMessage> result = chatRepository.findByProjectIdOrderByCreatedAtDesc(
                projectId,
                PageRequest.of(page, size)
        );

        List<ChatMessageResponse> content = result.getContent().stream()
                .map(ChatMessageResponse::from)
                .sorted(Comparator.comparing(ChatMessageResponse::getCreatedAt))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PagedChatResponse(
                content,
                result.getTotalPages(),
                result.getTotalElements()
        ));
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long projectId,
            @RequestBody MessageRequest request,
            Principal principal) {
        log.info("POST /api/projects/{}/messages - Sending new message", projectId);

        String content = request.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be empty");
        }
        if (content.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot exceed 1000 characters");
        }

        Project project = projectService.getProjectById(projectId);
        User sender = userService.getUserByEmail(principal.getName());

        ProjectChatMessage message = new ProjectChatMessage();
        message.setProject(project);
        message.setSender(sender);
        message.setContent(content.trim());
        message.setCreatedAt(LocalDateTime.now());

        ProjectChatMessage saved = chatRepository.save(message);
        ChatMessageResponse response = ChatMessageResponse.from(saved);

        broadcastMessage(projectId, response);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessages(@PathVariable Long projectId) {
        log.info("GET /api/projects/{}/messages/stream - Creating new SSE connection", projectId);
        projectService.getProjectById(projectId);

        SseEmitter emitter = new SseEmitter(1800000L);

        emitters.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>()).add(emitter);

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

    private void broadcastMessage(Long projectId, ChatMessageResponse message) {
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
