package com.example.taskmanager.dto.responses;

import com.example.taskmanager.entities.Team;
import com.example.taskmanager.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserResponse {
    private Long userId;
    private String userName;
    private String emailId;
    private UserRole role;
    private Team team;
}
