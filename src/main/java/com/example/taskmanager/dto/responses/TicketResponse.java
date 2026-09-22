package com.example.taskmanager.dto.responses;

import com.example.taskmanager.entities.Team;
import com.example.taskmanager.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long ticketId;
    private Team forTeam;
    private Team fromTeam;
    private UserResponse fromUser;
    private UserResponse assignedUser;
    private TicketStatus ticketStatus;
    private LocalDateTime createdAt;
}
