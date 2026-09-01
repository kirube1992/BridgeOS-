package com.bridgeos.backend.repository;

import com.bridgeos.backend.entity.ProjectChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectChatMessageRepository extends JpaRepository<ProjectChatMessage, Long> {
    Page<ProjectChatMessage> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);
}
