package com.bridgeos.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_item")
public class WorkItem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "business_context_notes", length = 1000)
    private String businessContextNotes;

    @Column(name = "acceptance_criteria", length = 1000)
    private String acceptanceCriteria;

    @Column(name= "clarity_score")
    private Integer clarityScore;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "work_item_status")
    @Enumerated(EnumType.STRING)
    private WorkItemStatus status = WorkItemStatus.TODO;

    @Column(name="dead_line")
    private LocalDate deadline;

    @Column(name ="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private  LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="assigned_to", nullable = true)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
