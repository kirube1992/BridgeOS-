package com.bridgeos.backend.DTO;

import lombok.Data;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private String businessContextNotes;
    private Integer clarityScore;
    private String status;
    private Long projectId;  // ← Reference by ID, not the whole object
    private String assignedToName;  // ← Only what you need
}