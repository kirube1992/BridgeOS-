package com.bridgeos.backend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AiSaveTranslationRequest {
    private String originalText;
    private String whatToBuild;
    private String whyItMatters;
    private List<String> acceptanceCriteria;
    private List<String> edgeCases;
    private String technicalNotes;
    private Long projectId;
}
