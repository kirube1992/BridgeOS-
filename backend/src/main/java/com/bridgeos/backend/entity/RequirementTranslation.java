package com.bridgeos.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "requirement_translation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequirementTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_text", nullable = false, length = 4000)
    private String originalText;

    @Column(name = "what_to_build", nullable = false, length = 4000)
    private String whatToBuild;

    @Column(name = "why_it_matters", length = 4000)
    private String whyItMatters;

    @Column(name = "acceptance_criteria", length = 4000)
    private String acceptanceCriteria;

    @Column(name = "edge_cases", length = 4000)
    private String edgeCases;

    @Column(name = "technical_notes", length = 4000)
    private String technicalNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
