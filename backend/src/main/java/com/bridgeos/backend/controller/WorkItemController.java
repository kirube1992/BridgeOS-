package com.bridgeos.backend.controller;

import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.entity.WorkItemStatus;
import com.bridgeos.backend.service.WorkItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/work-items")  // ← FIXED: Added 's'
public class WorkItemController {

    private final WorkItemService workItemService;

    // CREATE
    @PostMapping
    public ResponseEntity<WorkItem> createWorkItem(
            @Valid @RequestBody WorkItem workItem,
            @RequestParam Long projectId,
            @RequestParam Long createdByUserID,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long assignedToUserId) {

        log.info("POST /api/work-items - Creating work item: {}", workItem.getTitle());
        WorkItem createdWorkItem = workItemService.createWorkItem(
                workItem, projectId, createdByUserID, departmentId, assignedToUserId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkItem);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<WorkItem>> getAllWorkItems() {
        log.info("GET /api/work-items - Fetching all work items");
        List<WorkItem> workItems = workItemService.getAllWorkItem();
        return ResponseEntity.ok(workItems);
    }

    // READ BY PROJECT
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<WorkItem>> getWorkItemsByProject(@PathVariable Long projectId) {
        log.info("GET /api/work-items/project/{} - Fetching work items", projectId);
        List<WorkItem> workItems = workItemService.getWorkItemsByProject(projectId);
        return ResponseEntity.ok(workItems);
    }

    // READ BY ID
    @GetMapping("/{id}")  // ← FIXED: Removed dot
    public ResponseEntity<WorkItem> getWorkItemById(@PathVariable Long id) {
        log.info("GET /api/work-items/{} - Fetching work item", id);
        WorkItem workItem = workItemService.getWorkItemById(id);
        return ResponseEntity.ok(workItem);
    }

    // READ BY DEPARTMENT
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<WorkItem>> getWorkItemsByDepartment(@PathVariable Long departmentId) {
        log.info("GET /api/work-items/department/{} - Fetching work items", departmentId);
        List<WorkItem> workItems = workItemService.getWorkItemByProject(departmentId);
        return ResponseEntity.ok(workItems);
    }

    // READ BY ASSIGNEE
    @GetMapping("/assignee/{userId}")
    public ResponseEntity<List<WorkItem>> getWorkItemsByAssignee(@PathVariable Long userId) {
        log.info("GET /api/work-items/assignee/{} - Fetching work items", userId);
        List<WorkItem> workItems = workItemService.getWorkItemByAssignee(userId);
        return ResponseEntity.ok(workItems);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<WorkItem> updateWorkItem(
            @PathVariable Long id,
            @Valid @RequestBody WorkItem workItem) {
        log.info("PUT /api/work-items/{} - Updating work item", id);
        WorkItem updatedWorkItem = workItemService.updateWorkItem(id, workItem);
        return ResponseEntity.ok(updatedWorkItem);
    }

    // UPDATE STATUS
    @PatchMapping("/{id}/status")  // ← FIXED: Use PATCH for partial updates
    public ResponseEntity<WorkItem> updateStatus(
            @PathVariable Long id,
            @RequestParam WorkItemStatus status) {  // ← FIXED: Added @PathVariable and @RequestParam
        log.info("PATCH /api/work-items/{}/status - Updating status to {}", id, status);
        WorkItem updatedWorkItem = workItemService.updateStatus(id, status);
        return ResponseEntity.ok(updatedWorkItem);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkItem(@PathVariable Long id) {
        log.info("DELETE /api/work-items/{} - Deleting work item", id);
        workItemService.deleteWorkItem(id);
        return ResponseEntity.noContent().build();
    }
}