package com.example.taskmanager.dto.requests;

import com.example.taskmanager.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UpdateTicketRequest {
    private Long assignedUserId;
    private TicketStatus ticketStatus;
}
