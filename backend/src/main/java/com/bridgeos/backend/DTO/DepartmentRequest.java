package com.bridgeos.backend.DTO;

import com.bridgeos.backend.entity.DepartmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {
    @NotBlank(message = "Department name is required")
    @Size(max = 80, message = "Department name must be at most 80 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private String defaultWorkFlow;

    private Long departmentLeadId;

    private DepartmentStatus status = DepartmentStatus.ACTIVE;
}
