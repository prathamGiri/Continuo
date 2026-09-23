package com.example.taskmanager.dto.responses;

import com.example.taskmanager.enums.UserRole;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseWithTeam {
    private Long userId;
    private String userName;
    private String emailId;
    private UserRole role;
    private String teamName;
}
