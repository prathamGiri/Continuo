package com.example.taskmanager.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UpdateTicketAssignedUserRequest {
    @NotNull(message = "Must Provide a user to Assign the Ticket")
    private Long assignedUserId;
}
