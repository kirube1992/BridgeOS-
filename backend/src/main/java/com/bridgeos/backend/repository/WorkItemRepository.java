package com.bridgeos.backend.repository;


import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.entity.WorkItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    List<WorkItem>  findByProjectId(Long ProjectId);

    List<WorkItem> findByDepartmentId(Long deparmentId);

    List<WorkItem> findByAssignedToId(Long userId);

    List<WorkItem> findByStatus(WorkItemStatus status);

    List<WorkItem> findByProjectIdAndStatus(Long projectId, WorkItemStatus status);

    List<WorkItem> findByDepartmentIdAndStatus(Long departmentId, WorkItemStatus status);

    long countByDepartmentId(Long departmentId);
}
