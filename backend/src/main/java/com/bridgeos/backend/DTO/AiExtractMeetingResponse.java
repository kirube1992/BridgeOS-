package com.bridgeos.backend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AiExtractMeetingResponse {
    private List<AiActionItem> actionItems;
}
