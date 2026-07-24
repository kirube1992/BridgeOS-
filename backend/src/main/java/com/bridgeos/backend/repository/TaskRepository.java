package com.bridgeos.backend.repository;


import com.bridgeos.backend.entity.Task;
import com.bridgeos.backend.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject_Id(Long ProjectID);

    List<Task> findByAssignedTo_Id(Long userId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByProject_IdAndStatus(Long projectId, TaskStatus status);
}
