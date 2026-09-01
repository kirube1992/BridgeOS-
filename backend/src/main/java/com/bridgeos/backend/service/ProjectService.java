package com.bridgeos.backend.service;


import com.bridgeos.backend.DTO.ProjectAnalyticsDto;
import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.entity.ProjectStatus;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.entity.WorkItemStatus;
import com.bridgeos.backend.repository.ProjectRepository;
import com.bridgeos.backend.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkItemRepository workItemRepository;
    private final UserService  userService;

    @Transactional
    public   Project createProject(Project project, Long userId) {




        log.info("Creating a project {}", project.getName());
        User createdBy = userService.getUserById(userId);
        project.setCreatedBy(createdBy);
        project.setProjectManager(createdBy);


        if (project.getName() == null) {
            project.setName("Untitled Project");
        }

        if (project.getCreatedAt() == null) {
            project.setCreatedAt(LocalDateTime.now());
        }
        if (project.getUpdatedAt() == null) {
            project.setUpdatedAt(LocalDateTime.now());
        }

        // Default status if not set
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.ACTIVE);
        }
        return projectRepository.save(project);

    }

    public  List<Project> getAllProject() {
        log.info("fetch all project");
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
       log.info("fetching user by id {}", id);
       return projectRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Project not found by id: " + id));
    }



    @Transactional
    public Project updatProject(long id, Project updateProject) {
        log.info("Updating a project with id: {}", id);

      Project exstingProject = getProjectById(id);
      exstingProject.setName(updateProject.getName());
      exstingProject.setDescription(updateProject.getDescription());
      exstingProject.setClientContext(updateProject.getClientContext());
      exstingProject.setStatus(updateProject.getStatus());
      exstingProject.setDeadLine(updateProject.getDeadLine());
      return  projectRepository.save(exstingProject);

    }

    public void deleteProject(long id) {
        log.info("deleteing  a project with id: {}", id);

        getProjectById(id);

        projectRepository.deleteById(id);
    }
    public List<Project> getProjectsByUser(Long userId) {
        log.info("Fetching projects for user: {}", userId);
        return projectRepository.findByCreatedById(userId);
    }

    public List<ProjectAnalyticsDto> getProjectAnalytics(String period) {
        LocalDateTime since = switch (period == null ? "" : period.toUpperCase()) {
            case "WEEK" -> LocalDateTime.now().minusWeeks(1);
            case "MONTH" -> LocalDateTime.now().minusMonths(1);
            case "QUARTER" -> LocalDateTime.now().minusMonths(3);
            default -> null;
        };

        List<WorkItem> workItems = workItemRepository.findAll().stream()
                .filter(item -> since == null || item.getCreatedAt() == null || !item.getCreatedAt().isBefore(since))
                .toList();
        Map<Long, List<WorkItem>> tasksByProject = workItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProject().getId()));

        return projectRepository.findAll().stream().map(project -> {
            List<WorkItem> tasks = tasksByProject.getOrDefault(project.getId(), List.of());
            long completed = count(tasks, WorkItemStatus.DONE);
            double clarity = tasks.stream().filter(item -> item.getClarityScore() != null)
                    .mapToInt(item -> item.getClarityScore()).average().orElse(0);
            double resolutionDays = tasks.stream()
                    .filter(item -> item.getStatus() == WorkItemStatus.DONE && item.getCreatedAt() != null && item.getUpdatedAt() != null)
                    .mapToDouble(item -> java.time.Duration.between(item.getCreatedAt(), item.getUpdatedAt()).toHours() / 24.0)
                    .average().orElse(0);
            return new ProjectAnalyticsDto(
                    project.getId(), project.getName(), tasks.size(), completed,
                    count(tasks, WorkItemStatus.IN_PROGRESS), count(tasks, WorkItemStatus.REVIEW),
                    count(tasks, WorkItemStatus.TODO), clarity, resolutionDays);
        }).toList();
    }

    private long count(List<WorkItem> tasks, WorkItemStatus status) {
        return tasks.stream().filter(item -> item.getStatus() == status).count();
    }

}
