package com.bridgeos.backend.controller;


import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.entity.WorkItemStatus;
import com.bridgeos.backend.service.WorkItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.Resolution;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/work-item")
public class workItemController {

    private WorkItemService workItemService;

    @PostMapping
    public ResponseEntity<WorkItem> createWorkItem(@Valid @RequestBody WorkItem workItem, Long projectId, Long createdByUserID, @RequestBody(required = false) Long departmentID, @RequestBody(required = false) Long assignedToUserId) {
        log.info("POST api/work-item - crating work items{}:", workItem.getTitle());
        WorkItem  createWorkItem = workItemService.createWorkItem(workItem,projectId,createdByUserID,departmentID,assignedToUserId);
        return  ResponseEntity.status(HttpStatus.CREATED).body(createWorkItem);
    }


    @GetMapping("./{id}")
    public ResponseEntity<WorkItem> getWorkItemById(@PathVariable Long id) {
        log.info("GET api/work-item /{} Fetching work item", id);
        WorkItem  workItem  = workItemService.getWorkItemById(id);
        return ResponseEntity.ok(workItem);
    }

    @GetMapping("/porject/{projectId}")
    public ResponseEntity <List<WorkItem>> getWorkItemByDepartment(@PathVariable Long departmentId) {
        log.info("GET /api/work-items/department/{} - Fetching work items", departmentId);
        List<WorkItem> workItems = workItemService.getWorkItemsByProject(departmentId);
        return ResponseEntity.ok(workItems);
    }

    @GetMapping("/assignee/{userId}")
    public ResponseEntity <List<WorkItem>> getWorkItemByAssignee(@PathVariable  Long userId) {
        log.info("GET /api/work-items/assignee/{} - Fetching work items", userId);

        List<WorkItem>  workItems = workItemService.getWorkItemByAssignee(userId);

        return  ResponseEntity.ok(workItems);
    }


    @PutMapping("/{id}")
    public ResponseEntity<WorkItem> updateWorkItems(@PathVariable Long id,@Valid @RequestBody WorkItem  workItem) {
        WorkItem updatedWorkItem = workItemService.updateWorkItem(id, workItem);

        return  ResponseEntity.ok(updatedWorkItem);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<WorkItem> updateStatus(Long id, WorkItemStatus status){
        log.info("PATCH /api/work-items/{}/status - Updating status to {}", id, status);
        WorkItem updatedWorkItem = workItemService.updateStatus(id, status);
        return ResponseEntity.ok(updatedWorkItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkItem(@PathVariable Long id) {
        log.info("DELETE /api/work-items/{} - Deleting work item", id);
        workItemService.deleteWorkItem(id);
        return ResponseEntity.noContent().build();
    }
}
