package com.bridgeos.backend.service;

import com.bridgeos.backend.DTO.*;
import com.bridgeos.backend.entity.*;
import com.bridgeos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final RequirementTranslationRepository requirementTranslationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkItemRepository workItemRepository;
    private final DepartmentRepository departmentRepository;

    @Value("${app.ai.base-url:http://localhost:8090}")
    private String aiBaseUrl;

    @Value("${app.ai.timeout-ms:30000}")
    private int aiTimeoutMs;

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(aiTimeoutMs);
        factory.setReadTimeout(aiTimeoutMs);
        return new RestTemplate(factory);
    }

    private <T> T safePost(String path, Object body, Class<T> responseType, T fallback) {
        try {
            RestTemplate rt = buildRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<T> resp = rt.exchange(aiBaseUrl + path, HttpMethod.POST, entity, responseType);
            return resp.getBody() != null ? resp.getBody() : fallback;
        } catch (Exception e) {
            log.warn("AI sidecar call failed for {}: {}", path, e.getMessage());
            return fallback;
        }
    }

    private <T> T safeGet(String path, Class<T> responseType, T fallback) {
        try {
            RestTemplate rt = buildRestTemplate();
            ResponseEntity<T> resp = rt.getForEntity(aiBaseUrl + path, responseType);
            return resp.getBody() != null ? resp.getBody() : fallback;
        } catch (Exception e) {
            log.warn("AI sidecar health check failed: {}", e.getMessage());
            return fallback;
        }
    }

    public AiHealthResponse health() {
        Map<String, Object> raw = safeGet("/health", Map.class, null);
        AiHealthResponse resp = new AiHealthResponse();
        if (raw != null) {
            resp.setStatus((String) raw.get("status"));
            resp.setProvider((String) raw.get("provider"));
            resp.setModel((String) raw.get("model"));
            resp.setSidecarReachable(true);
        } else {
            resp.setStatus("unreachable");
            resp.setProvider("none");
            resp.setSidecarReachable(false);
        }
        return resp;
    }

    public AiTranslateResponse translate(AiTranslateRequest request, Long userId) {
        String projectName = null;
        if (request.getProjectId() != null) {
            projectName = projectRepository.findById(request.getProjectId())
                    .map(Project::getName).orElse(null);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("text", request.getText());
        body.put("projectId", request.getProjectId());
        body.put("projectName", projectName);

        AiTranslateResponse sidecarResp = safePost("/translate", body, AiTranslateResponse.class, null);
        if (sidecarResp == null) {
            sidecarResp = new AiTranslateResponse();
            sidecarResp.setOriginalText(request.getText());
            sidecarResp.setWhatToBuild("Implement the requested feature based on the requirement text.");
            sidecarResp.setWhyItMatters("This requirement addresses user/stakeholder needs.");
            sidecarResp.setAcceptanceCriteria(List.of(
                    "User can access the feature from the relevant UI",
                    "Data is persisted and visible",
                    "Error states are handled gracefully"
            ));
            sidecarResp.setEdgeCases(List.of(
                    "Empty/malformed input is rejected",
                    "Concurrent edits do not corrupt data"
            ));
            sidecarResp.setTechnicalNotes("Use existing BridgeOS patterns.");
        }
        return sidecarResp;
    }

    public RequirementTranslation saveTranslation(AiSaveTranslationRequest request, Long userId) {
        RequirementTranslation entity = new RequirementTranslation();
        entity.setOriginalText(request.getOriginalText());
        entity.setWhatToBuild(request.getWhatToBuild());
        entity.setWhyItMatters(request.getWhyItMatters());
        entity.setAcceptanceCriteria(String.join("\n", request.getAcceptanceCriteria() != null ? request.getAcceptanceCriteria() : List.of()));
        entity.setEdgeCases(String.join("\n", request.getEdgeCases() != null ? request.getEdgeCases() : List.of()));
        entity.setTechnicalNotes(request.getTechnicalNotes());
        entity.setCreatedAt(LocalDateTime.now());

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
        entity.setCreatedBy(creator);

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + request.getProjectId()));
            entity.setProject(project);
        }

        return requirementTranslationRepository.save(entity);
    }

    public List<RequirementTranslation> listTranslations(Long userId) {
        return requirementTranslationRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);
    }

    public AiExtractMeetingResponse extractMeeting(AiExtractMeetingRequest request) {
        List<AiUserRef> userRefs = new ArrayList<>();
        if (request.getProjectId() != null) {
            List<User> users = userRepository.findAll();
            for (User u : users) {
                AiUserRef ref = new AiUserRef();
                ref.setId(u.getId());
                ref.setName(u.getName());
                ref.setEmail(u.getEmail());
                userRefs.add(ref);
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("notes", request.getNotes());
        body.put("projectId", request.getProjectId());
        body.put("users", userRefs);

        AiExtractMeetingResponse resp = safePost("/extract-meeting", body, AiExtractMeetingResponse.class, null);
        if (resp == null || resp.getActionItems() == null) {
            resp = new AiExtractMeetingResponse();
            resp.setActionItems(List.of());
        }
        return resp;
    }

    public WorkItem promoteActionToTask(AiPromoteActionRequest request) {
        WorkItem workItem = new WorkItem();

        String description = request.getDescription() != null ? request.getDescription().trim() : "New task from meeting";
        workItem.setTitle(description.length() > 120 ? description.substring(0, 117) + "..." : description);
        workItem.setDescription(description);
        workItem.setBusinessContextNotes("Created from AI-extracted meeting action item.");

        try {
            Priority priority = Priority.valueOf(request.getPriority() != null ? request.getPriority().toUpperCase() : "MEDIUM");
            workItem.setPriority(priority);
        } catch (Exception ignored) {
            workItem.setPriority(Priority.MEDIUM);
        }

        workItem.setStatus(WorkItemStatus.TODO);

        if (request.getDueDate() != null && !request.getDueDate().isBlank()) {
            try {
                workItem.setDeadline(LocalDate.parse(request.getDueDate()));
            } catch (Exception ignored) {
            }
        }

        workItem.setCreatedAt(LocalDateTime.now());
        workItem.setUpdatedAt(LocalDateTime.now());
        workItem.setAcceptanceCriteria("Complete the described action and verify with stakeholder.");
        workItem.setClarityScore(50);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));
        workItem.setProject(project);

        User creator = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid creator user ID"));
        workItem.setCreatedBy(creator);

        if (request.getAssignedToUserId() != null) {
            userRepository.findById(request.getAssignedToUserId()).ifPresent(workItem::setAssignedTo);
        }

        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId()).ifPresent(workItem::setDepartment);
        } else if (project != null && creator.getDepartment() != null) {
            workItem.setDepartment(creator.getDepartment());
        }

        return workItemRepository.save(workItem);
    }

    public AiAskResponse ask(AiAskRequest request) {
        List<AiAskContextItem> context = request.getContext() != null ? request.getContext() : new ArrayList<>();

        if (context.isEmpty() && request.getProjectId() != null) {
            List<WorkItem> items = workItemRepository.findByProjectId(request.getProjectId());
            for (WorkItem wi : items) {
                AiAskContextItem ci = new AiAskContextItem();
                ci.setType("work_item");
                ci.setId(wi.getId());
                ci.setSummary(String.format("[%s] %s - %s (assigned: %s)",
                        wi.getStatus(), wi.getTitle(),
                        wi.getDescription() != null ? wi.getDescription() : "",
                        wi.getAssignedTo() != null ? wi.getAssignedTo().getName() : "unassigned"));
                context.add(ci);
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("question", request.getQuestion());
        body.put("projectId", request.getProjectId());
        body.put("context", context);

        AiAskResponse resp = safePost("/ask", body, AiAskResponse.class, null);
        if (resp == null) {
            resp = new AiAskResponse();
            resp.setQuestion(request.getQuestion());
            resp.setAnswer("I cannot answer right now. Please try again later.");
            resp.setSources(List.of());
        }
        return resp;
    }
}
