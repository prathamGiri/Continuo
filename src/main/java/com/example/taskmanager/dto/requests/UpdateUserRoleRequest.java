package com.example.taskmanager.dto.requests;

import com.example.taskmanager.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UpdateUserRoleRequest {
    @NotNull(message = "Must Provide a Role.")
    private UserRole newRole;
}
