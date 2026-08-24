package com.bridgeos.backend.DTO;

import lombok.Data;

@Data
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private String clientContext;
    private String status;
    // NO tasks list here! ← This prevents the loop
}