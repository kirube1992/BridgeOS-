package com.bridgeos.backend.DTO;

import lombok.Data;

@Data
public class AiPromoteActionRequest {
    private String description;
    private Long assignedToUserId;
    private String dueDate;
    private String priority;
    private Long projectId;
    private Long createdByUserId;
    private Long departmentId;
}
