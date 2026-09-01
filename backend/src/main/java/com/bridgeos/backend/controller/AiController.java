package com.bridgeos.backend.controller;

import com.bridgeos.backend.DTO.*;
import com.bridgeos.backend.entity.RequirementTranslation;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.service.AiService;
import com.bridgeos.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;
    private final UserService userService;

    private Long resolveUserId(UserDetails principal) {
        if (principal == null || principal.getUsername() == null) {
            return null;
        }
        try {
            User user = userService.getUserByEmail(principal.getUsername());
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            log.warn("Failed to resolve user from principal: {}", e.getMessage());
            return null;
        }
    }

    @GetMapping("/health")
    public ResponseEntity<AiHealthResponse> health() {
        log.info("GET /api/ai/health");
        return ResponseEntity.ok(aiService.health());
    }

    @PostMapping("/translate")
    public ResponseEntity<AiTranslateResponse> translate(
            @RequestBody AiTranslateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        log.info("POST /api/ai/translate - text length: {}",
                request.getText() != null ? request.getText().length() : 0);
        Long userId = resolveUserId(principal);
        AiTranslateResponse response = aiService.translate(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate/save")
    public ResponseEntity<RequirementTranslation> saveTranslation(
            @RequestBody AiSaveTranslationRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        log.info("POST /api/ai/translate/save");
        Long userId = resolveUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RequirementTranslation saved = aiService.saveTranslation(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/translate/my")
    public ResponseEntity<List<RequirementTranslation>> myTranslations(
            @AuthenticationPrincipal UserDetails principal) {
        log.info("GET /api/ai/translate/my");
        Long userId = resolveUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(aiService.listTranslations(userId));
    }

    @PostMapping("/extract-meeting")
    public ResponseEntity<AiExtractMeetingResponse> extractMeeting(
            @RequestBody AiExtractMeetingRequest request) {
        log.info("POST /api/ai/extract-meeting - notes length: {}",
                request.getNotes() != null ? request.getNotes().length() : 0);
        return ResponseEntity.ok(aiService.extractMeeting(request));
    }

    @PostMapping("/extract-meeting/promote")
    public ResponseEntity<WorkItem> promoteActionToTask(
            @RequestBody AiPromoteActionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        log.info("POST /api/ai/extract-meeting/promote - description: {}",
                request.getDescription() != null
                        ? request.getDescription().substring(0, Math.min(40, request.getDescription().length()))
                        : "");
        Long userId = resolveUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (request.getCreatedByUserId() == null) {
            request.setCreatedByUserId(userId);
        }
        WorkItem created = aiService.promoteActionToTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/ask")
    public ResponseEntity<AiAskResponse> ask(@RequestBody AiAskRequest request) {
        log.info("POST /api/ai/ask - question: {}",
                request.getQuestion() != null
                        ? request.getQuestion().substring(0, Math.min(60, request.getQuestion().length()))
                        : "");
        return ResponseEntity.ok(aiService.ask(request));
    }
}
