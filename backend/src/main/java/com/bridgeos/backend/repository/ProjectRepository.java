package com.bridgeos.backend.repository;

import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByCreatedById(Long UserID);

    List<Project> findByNameContiningIgnoreCase(String name);

}

