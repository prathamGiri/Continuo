package com.example.taskmanager.dto.requests;

import com.example.taskmanager.enums.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CreateTicketRequest {

    @NotNull(message = "Must Provide a Team")
    private Long forTeamId;
    @NotNull(message = "Must Provide a Team")
    private Long fromTeamId;
    @NotNull(message = "Must Provide a User")
    private Long fromUserId;
}
