package com.example.taskmanager.dto.requests;

import com.example.taskmanager.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UpdateUserRequest {
    private UserRole newRole;
    private Long newTeamId;
}
