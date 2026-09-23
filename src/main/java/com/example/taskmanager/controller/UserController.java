package com.example.taskmanager.controller;

import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.dto.requests.UpdateUserRequest;
import com.example.taskmanager.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody User user){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUserTeam(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request
    ){
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }
}
