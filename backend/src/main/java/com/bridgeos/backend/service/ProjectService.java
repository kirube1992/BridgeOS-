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
}
