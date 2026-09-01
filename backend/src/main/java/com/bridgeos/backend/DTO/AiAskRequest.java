package com.bridgeos.backend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AiAskRequest {
    private String question;
    private Long projectId;
    private List<AiAskContextItem> context;
}
