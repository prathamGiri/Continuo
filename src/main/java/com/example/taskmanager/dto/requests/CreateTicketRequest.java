package com.example.taskmanager.dto.requests;

import com.example.taskmanager.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CreateTicketRequest {
    private Long forTeamId;
    private Long fromTeamId;
    private Long fromUserId;
    private TicketStatus ticketStatus;
}
