package com.bridgeos.backend.DTO;

import lombok.Data;

@Data
public class AiExtractMeetingRequest {
    private String notes;
    private Long projectId;
}
