package com.bridgeos.backend.repository;


import com.bridgeos.backend.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditEvent> findByProjectId(Long projectId);

    List<AuditEvent> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<AuditEvent> findByActorId(Long actorId);

//    @Query("SELECT a FROM AuditEvent a WHERE " +
//            "LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//            "CAST(a.detail AS text) LIKE LOWER(CONCAT('%', :keyword, '%'))")
//    List<AuditEvent> searchByKeyword(String keyword);

    @Query(value = """
    SELECT *
    FROM audit_event
    WHERE LOWER(summary) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR CAST(detail AS TEXT) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """, nativeQuery = true)
    List<AuditEvent> searchByKeyword(String keyword);

    List<AuditEvent>  findByProjectIdAndCreatedAtBetween(Long porjectId, LocalDateTime start, LocalDateTime end);
}


