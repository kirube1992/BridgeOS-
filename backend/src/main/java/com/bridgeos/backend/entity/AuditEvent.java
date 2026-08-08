package com.bridgeos.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "audit_event")
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private  String eventType;

    @Column(name = "entity_type")
    private  String entityType;


    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "summery", nullable = false)
    private String summery;

    @Column(name = "detail", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id" )
    private User actor;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
