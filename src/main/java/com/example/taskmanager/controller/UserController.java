package com.example.taskmanager.controller;

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
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public User getUserById(@PathVariable Long userId){
        return userService.getUserById(userId);
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user){
        return userService.createUser(user);
    }

    @PatchMapping("/users/{userId}")
    public User updateUserTeam(@PathVariable Long userId, @RequestBody UpdateUserRequest request){
        User updatedUser = null;
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
