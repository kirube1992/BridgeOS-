package com.bridgeos.backend.service;


import com.bridgeos.backend.entity.*;
import com.bridgeos.backend.repository.WorkItemRepository;
import jdk.dynalink.linker.LinkerServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemService {

    private final WorkItemRepository workItemRepository;
    private final DepartmentService departmentService;
    private final ProjectService projectService;
    private final UserService  userService;


    public WorkItem createWorkItem(WorkItem workItem, Long projectId, Long assignedToUserId, Long createdByUserId, Long departmentId) {

        log.info("Creating work item {}",workItem.getTitle());

        Project project = projectService.getProjectById((projectId));
        workItem.setProject(project);


        if (assignedToUserId != null){
            User assignedTo = userService.getUserById(assignedToUserId);
            workItem.setAssignedTo(assignedTo);
        }

        User createdBy = userService.getUserById(createdByUserId);
        workItem.setCreatedBy(createdBy);


        if(departmentId != null) {
            workItem.setDepartment(departmentService.getDepartmentById(departmentId));
        } else {
            workItem.setDepartment(departmentService.getDepartmentById(1l));
        }

        if(workItem.getCreatedAt() == null){
            workItem.setCreatedAt(LocalDateTime.now());
        }
        if(workItem.getUpdatedAt() == null) {
            workItem.setUpdatedAt((LocalDateTime.now()));
        }

        calculateClarityScore(workItem);
        return  workItemRepository.save(workItem);

    }

    public List<WorkItem> getAllWorkItem(){
        log.info("Fetching all work item");
        return workItemRepository.findAll();
    }
    public WorkItem getWorkItemById(Long id){
        log.info("Fetching work item by id:{}", id);
        return  workItemRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("work item not found  with id:" + id));
    }

    public List<WorkItem> getWorkItemsByProject(Long projectId){
        log.info("Fetching work by projectId:{}", projectId);
        return  workItemRepository.findByProjectId(projectId);
    }


    public List<WorkItem> getWorkItemByProject(Long departmentId) {
        log.info("Fetch workItems by departmentId: {}", departmentId);

        return workItemRepository.findByDepartmentId(departmentId);
    }

    public List<WorkItem> getWorkItemByAssignee(Long userID) {
        log.info("Fetching work items assign to user: {}", userID);

        return  workItemRepository.findByAssignedToId(userID);
    }


    @Transactional
    public WorkItem updateWorkItem(Long id, WorkItem updatingWorkItem) {
        log.info("updating work item with id: {}", id);

        WorkItem exstingWorkItem = getWorkItemById(id);


        exstingWorkItem.setTitle(updatingWorkItem.getTitle());
        exstingWorkItem.setDescription(updatingWorkItem.getDescription());
        exstingWorkItem.setBusinessContextNotes(updatingWorkItem.getBusinessContextNotes());
        exstingWorkItem.setAcceptanceCriteria(updatingWorkItem.getAcceptanceCriteria());
        exstingWorkItem.setStatus(updatingWorkItem.getStatus());
        exstingWorkItem.setPriority(updatingWorkItem.getPriority());
        exstingWorkItem.setDeadline(updatingWorkItem.getDeadline());
        exstingWorkItem.setUpdatedAt(updatingWorkItem.getUpdatedAt());

        calculateClarityScore(exstingWorkItem);

        if(updatingWorkItem.getAssignedTo() != null) {
            User newAssignee = userService.getUserById(updatingWorkItem.getAssignedTo().getId());
            exstingWorkItem.setAssignedTo(newAssignee);
        }

        return workItemRepository.save(exstingWorkItem);
    }

    @Transactional
    public void deleteWorkItem(long id) {
        log.info("deleting work item with id:{}", id);

        getWorkItemById(id);
        workItemRepository.deleteById(id);
    }

    @Transactional
    public WorkItem updateStatus(Long id, WorkItemStatus status) {
        log.info("updating status of work items");
        WorkItem workItem = getWorkItemById(id);

        workItem.setStatus(status);
        workItem.setUpdatedAt(LocalDateTime.now());
        return workItemRepository.save(workItem);
    }

    private void calculateClarityScore(WorkItem workItem) {
        int score = 0;
        if (workItem.getBusinessContextNotes() != null && !workItem.getBusinessContextNotes().isEmpty()) {
            score += 25;
        }
        if (workItem.getAcceptanceCriteria() != null && !workItem.getAcceptanceCriteria().isEmpty()) {
            score += 25;
        }
        if (workItem.getDeadline() != null) {
            score += 15;
        }
        if (workItem.getTitle() != null && workItem.getTitle().length() >= 10) {
            score += 20;
        }
        if (workItem.getDescription() != null && workItem.getDescription().length() >= 20) {
            score += 15;
        }
        workItem.setClarityScore(Math.min(100, score));
    }
}
