package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.User;

public class UserMapper {
    public static UserResponse toDTO(User user){
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .emailId(user.getEmailId())
                .role(user.getRole())
                .team(user.getTeam())
                .build();
    }
}
