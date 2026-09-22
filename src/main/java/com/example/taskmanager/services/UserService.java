package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.UpdateUserRequest;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.User;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long userId);
    UserResponse createUser(User user);
    UserResponse updateUserTeam(Long userId, UpdateUserRequest request);
    UserResponse updateUserRole(Long userId, UpdateUserRequest request);
    UserResponse removeUserFromTeam(Long userId, UpdateUserRequest request);
}
