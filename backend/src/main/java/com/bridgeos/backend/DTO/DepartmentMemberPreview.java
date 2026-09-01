package com.bridgeos.backend.DTO;

import com.bridgeos.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentMemberPreview {
    private Long id;
    private String name;
    private String email;
    private String role;

    public static DepartmentMemberPreview from(User user) {
        return new DepartmentMemberPreview(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
