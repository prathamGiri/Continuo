package com.example.taskmanager.controller;

import com.example.taskmanager.dto.requests.UpdateUserRoleRequest;
import com.example.taskmanager.dto.requests.UpdateUserTeamRequest;
import com.example.taskmanager.dto.responses.UserResponseWithTeam;
import com.example.taskmanager.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseWithTeam>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseWithTeam> getUserById(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping("/{userId}/team")
    public ResponseEntity<UserResponseWithTeam> updateUserTeam(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserTeamRequest request
    ){
        return ResponseEntity.ok(userService.updateUserTeam(userId, request));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponseWithTeam> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ){
        return ResponseEntity.ok(userService.updateUserRole(userId, request));
    }

    @PatchMapping("/{userId}/team/remove")
    public ResponseEntity<UserResponseWithTeam> removeUserFromTeam(
            @PathVariable Long userId
    ){
        return ResponseEntity.ok(userService.removeUserFromTeam(userId));
    }
}
