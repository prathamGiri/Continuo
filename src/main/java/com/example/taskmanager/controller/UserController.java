package com.example.taskmanager.controller;

import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.dto.requests.UpdateUserRequest;
import com.example.taskmanager.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public UserResponse getUserById(@PathVariable Long userId){
        return userService.getUserById(userId);
    }

    @PostMapping("/users")
    public UserResponse createUser(@RequestBody User user){
        return userService.createUser(user);
    }

    @PatchMapping("/users/{userId}")
    public UserResponse updateUserTeam(@PathVariable Long userId, @RequestBody UpdateUserRequest request){
        UserResponse updatedUser = null;
        boolean updaterd = false;
        if (request.getNewRole() != null){
            updatedUser = userService.updateUserRole(userId, request);
            updaterd=true;
        }
        if (request.getNewTeamId() != null){
            updatedUser = userService.updateUserTeam(userId, request);
            updaterd=true;
        }
        if (!updaterd){
            updatedUser = userService.getUserById(userId);
        }
        return updatedUser;
    }
}
