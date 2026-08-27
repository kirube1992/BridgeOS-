package com.bridgeos.backend.repository;

import com.bridgeos.backend.entity.ProjectChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectChatMessageRepository extends JpaRepository<ProjectChatMessage, Long> {
    List<ProjectChatMessage> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}
