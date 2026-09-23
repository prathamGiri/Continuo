package com.example.taskmanager.dto.requests;

import com.example.taskmanager.enums.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UpdateTicketStatusRequest {
    @NotNull(message = "Must Provide New Ticket Status.")
    private TicketStatus ticketStatus;
}
