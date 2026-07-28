package com.bridgeos.backend.service;


import com.bridgeos.backend.entity.Department;
import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.repository.WorkItemRepository;
import jdk.dynalink.linker.LinkerServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
