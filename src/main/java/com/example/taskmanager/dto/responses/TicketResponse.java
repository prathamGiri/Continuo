package com.example.taskmanager.dto.responses;

import com.example.taskmanager.enums.TicketPriority;
import com.example.taskmanager.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long ticketId;
    private String title;
    private String description;
    private TicketPriority priority;
    private LocalDate dueDate;
    private boolean overdue;
    private TeamResponse forTeam;
    private TeamResponse fromTeam;
    private UserResponse fromUser;
    private UserResponse assignedUser;
    private TicketStatus ticketStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
