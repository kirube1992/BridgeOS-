package com.bridgeos.backend.DTO;

import com.bridgeos.backend.entity.Department;
import com.bridgeos.backend.entity.DepartmentStatus;
import com.bridgeos.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String name;
    private String description;
    private String defaultWorkFlow;
    private DepartmentStatus status;
    private DepartmentMemberPreview departmentLead;
    private long memberCount;
    private long workItemCount;
    private List<DepartmentMemberPreview> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepartmentResponse from(
            Department department,
            long memberCount,
            long workItemCount,
            List<DepartmentMemberPreview> members
    ) {
        DepartmentMemberPreview lead = null;
        User leadUser = department.getDepartmentLead();
        if (leadUser != null) {
            lead = DepartmentMemberPreview.from(leadUser);
        }

        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getDefaultWorkFlow(),
                department.getStatus(),
                lead,
                memberCount,
                workItemCount,
                members,
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
