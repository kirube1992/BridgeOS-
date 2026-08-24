package com.bridgeos.backend.controller;


import com.bridgeos.backend.DTO.ProjectDto;
import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.service.ProjectService;
import com.bridgeos.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
@Slf4j
public class ProjectController {

    private final ProjectService projectService;
    private  final UserService userService;


    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody  @Valid Project project) {

        log.info("POST api/project creating project: {} ", project.getName());

        long userId = 1L;
        Project createProject = projectService.createProject(project, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createProject);

    }

    @GetMapping
//    public ResponseEntity<List<Project>>  getAllProject() {
//        log.info("./api/projects  fetch all projects");
//        List<Project> projects = projectService.getAllProject();
//        return ResponseEntity.ok(projects);
//    }

    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        List<Project> projects = projectService.getAllProject();
        List<ProjectDto> dtos = projects.stream()
                .map(this::toProjectDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private ProjectDto toProjectDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setClientContext(project.getClientContext());
        dto.setStatus(project.getStatus().name());
        // No tasks list!
        return dto;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectByID(@PathVariable long id){
        log.info("./api/projects/id fetch project by id: {}", id );
        Project project = projectService.getProjectById(id);
        return  ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject( @PathVariable Long id, @Valid @RequestBody Project project) {
        log.info("PUT api/project/{} updating a project", id);
         Project updateProject = projectService.updatProject(id, project);

         return  ResponseEntity.ok(updateProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id){
        log.info("delete project {}", id);

        projectService.deleteProject(id);


        return ResponseEntity.noContent().build();

    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Project>> getProjectsByUser(@PathVariable Long userId) {
        log.info("GET /api/projects/user/{} - Fetching projects", userId);
        List<Project> projects = projectService.getProjectsByUser(userId);
        return ResponseEntity.ok(projects);
    }



}
