package com.example.taskmanager.dto.requests;

import com.example.taskmanager.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UpdateUserTeamRequest {
    @NotNull(message = "Must provide a team to add the user.")
    private Long newTeamId;
}
