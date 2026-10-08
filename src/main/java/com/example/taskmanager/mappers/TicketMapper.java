package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.TicketStatus;

import java.time.LocalDate;

public class TicketMapper {
    public static TicketResponse toDTO(Ticket ticket){
        UserResponse forUser = ticket.getAssignedUser() == null ? null : UserMapper.toDTOWithoutTeam(ticket.getAssignedUser());
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .dueDate(ticket.getDueDate())
                .overdue(isOverdue(ticket))
                .forTeam(TeamMapper.teamDTO(ticket.getForTeam()))
                .fromTeam(TeamMapper.teamDTO(ticket.getFromTeam()))
                .ticketStatus(ticket.getTicketStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .closedAt(ticket.getClosedAt())
                .fromUser(UserMapper.toDTOWithoutTeam(ticket.getFromUser()))
                .assignedUser(forUser)
                .build();
    }

    public static Ticket toEntity(CreateTicketRequest request, Team forTeam, Team fromTeam, User fromUser){
        return Ticket.builder()
                .title(request.getTitle().strip())
                .description(request.getDescription())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .forTeam(forTeam)
                .fromTeam(fromTeam)
                .fromUser(fromUser)
                .build();
    }

    private static boolean isOverdue(Ticket ticket){
        return ticket.getDueDate() != null
                && ticket.getTicketStatus() != TicketStatus.Done
                && ticket.getDueDate().isBefore(LocalDate.now());
    }
}
