package com.example.taskmanager.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CreateTeamRequest {
    @NotBlank(message = "Must provide a Name")
    private String teamName;
}
