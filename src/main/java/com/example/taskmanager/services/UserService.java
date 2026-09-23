package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.UpdateUserRequest;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.User;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long userId);
    UserResponse createUser(User user);
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    UserResponse removeUserFromTeam(Long userId, UpdateUserRequest request);
}
