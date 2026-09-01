package com.bridgeos.backend.DTO;

import lombok.Data;

@Data
public class AiSuggestedAssignee {
    private Long id;
    private String name;
    private double confidence;
}
