package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateUserRequest;
import com.example.taskmanager.dto.requests.LoginRequest;
import com.example.taskmanager.dto.requests.UpdateUserRoleRequest;
import com.example.taskmanager.dto.requests.UpdateUserTeamRequest;
import com.example.taskmanager.dto.responses.AuthResponse;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.dto.responses.UserResponseWithTeam;

import java.util.List;

public interface UserService {
    List<UserResponseWithTeam> getAllUsers();
    UserResponseWithTeam getUserById(Long userId);
    UserResponse createUser(CreateUserRequest request);
    UserResponseWithTeam updateUserRole(Long userId, UpdateUserRoleRequest request);
    UserResponseWithTeam updateUserTeam(Long userId, UpdateUserTeamRequest request);
    UserResponseWithTeam removeUserFromTeam(Long userId);
    AuthResponse loginUser(LoginRequest request);
}
