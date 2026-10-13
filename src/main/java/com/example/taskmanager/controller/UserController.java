package com.example.taskmanager.controller;

import com.example.taskmanager.dto.requests.UpdateUserRoleRequest;
import com.example.taskmanager.dto.requests.UpdateUserTeamRequest;
import com.example.taskmanager.dto.responses.UserResponseWithTeam;
import com.example.taskmanager.security.CurrentUser;
import com.example.taskmanager.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('SysAdmin')")
    public ResponseEntity<List<UserResponseWithTeam>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseWithTeam> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping("/{userId}/team")
    @PreAuthorize("hasAnyRole('SysAdmin', 'TeamAdmin')")
    public ResponseEntity<UserResponseWithTeam> updateUserTeam(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserTeamRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserTeam(CurrentUser.id(jwt), userId, request));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('SysAdmin')")
    public ResponseEntity<UserResponseWithTeam> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserRole(userId, request));
    }

    @PatchMapping("/{userId}/team/remove")
    @PreAuthorize("hasAnyRole('SysAdmin', 'TeamAdmin')")
    public ResponseEntity<UserResponseWithTeam> removeUserFromTeam(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(userService.removeUserFromTeam(CurrentUser.id(jwt), userId));
    }
}
