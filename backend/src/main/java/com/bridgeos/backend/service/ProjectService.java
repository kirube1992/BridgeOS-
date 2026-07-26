package com.bridgeos.backend.service;


import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.repository.ProjectRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService  userService;

    @Transactional
    private  Project createProject(Project project, Long userId) {

        log.info("Creating a project {}", project.getName());
        User createdBy = userService.getUserById(userId);
        project.setCreatedBy(createdBy);

        return projectRepository.save(project);

    }

    public  List<Project> getAllProject() {
        log.info("fetch all project");
        return projectRepository.findAll();
    }

    public Project getProjectById(long id) {
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

}
