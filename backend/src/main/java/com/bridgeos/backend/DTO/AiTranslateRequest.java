package com.bridgeos.backend.DTO;

import lombok.Data;

@Data
public class AiTranslateRequest {
    private String text;
    private Long projectId;
}
