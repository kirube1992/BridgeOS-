package com.bridgeos.backend.DTO;

import lombok.Data;

@Data
public class AiActionItem {
    private String description;
    private AiSuggestedAssignee suggestedAssignee;
    private String suggestedDueDate;
    private String priority;
}
