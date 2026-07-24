package com.bridgeos.backend.entity;


import jakarta.persistence.*;
import lombok.Data;
import com.bridgeos.backend.entity.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "Projects" )
public class Project {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String title;
    @Column(nullable = false, name = "status")
    @Enumerated(EnumType.STRING)
    private ProjectStatus  status = ProjectStatus.ACTIVE;
    @Column(nullable = false)
    private String projectManager;
    @Column(length = 500)
    private String description;

    @Column(name="clinet_context", length = 1000)
    private String clientContext;

    @Column(name ="deadline")
    private LocalDate deadLine;

    @Column(name="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();


    @JoinColumn(name="created_by", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;


    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();
}