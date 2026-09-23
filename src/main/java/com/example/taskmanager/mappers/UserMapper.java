package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.requests.CreateUserRequest;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.dto.responses.UserResponseWithTeam;
import com.example.taskmanager.entities.User;

public class UserMapper {
    public static UserResponseWithTeam toDTOWithTeam(User user){
        String teamName = user.getTeam() == null ? null : user.getTeam().getTeamName();
        return UserResponseWithTeam.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .emailId(user.getEmailId())
                .role(user.getRole())
                .teamName(teamName)
                .build();
    }

    public static UserResponse toDTOWithoutTeam(User user){
        String teamName = user.getTeam() == null ? null : user.getTeam().getTeamName();
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .emailId(user.getEmailId())
                .role(user.getRole())
                .build();
    }

    public static User toEntity(CreateUserRequest request){
        return User.builder()
                .userName(request.getUserName())
                .emailId(request.getEmailId())
                .password(request.getPassword())
                .build();
    }
}
