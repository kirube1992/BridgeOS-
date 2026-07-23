package com.bridgeos.backend.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "Projects" )
public class Project {


    @id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String title;
    @Column(nullable = false, name = "status")
    @Enumerated(EnumType.STRING)
    private String status;
    @Column(nullable = false)
    private String projectManager;
    @Column(length = 500)
    private String description;

    @Column(name="clinet_context", length = 1000)
    private String clientContext;

    @Column(name ="deadline")
    private LocalDate deadLine;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}



 enum  projectStatus {
    ACTIVE,
     ON_HOLD,
     COMPLETED,
     ARCHIVED,
     ONGOING
 }

