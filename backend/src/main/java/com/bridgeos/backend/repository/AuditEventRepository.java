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

    List<AuditEvent> findByProjectID(Long projectiD);

    List<AuditEvent> findByActorId(Long actorId);

    @Query("SELECT a FROM AuditEvent a WHERE " +
            "LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(a.detail AS text) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AuditEvent> searchByKeyword(String keyword);

    List<AuditEvent> seracByKeyWord(String keyword);

    List<AuditEvent>  findByProjectIdAndAtBetween(Long porjectId, LocalDateTime start, LocalDateTime end);

}
