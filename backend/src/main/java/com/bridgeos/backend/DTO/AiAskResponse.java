package com.bridgeos.backend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AiAskResponse {
    private String question;
    private String answer;
    private List<AiAskSource> sources;
}
