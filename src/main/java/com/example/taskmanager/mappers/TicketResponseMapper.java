package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.Ticket;

public class TicketResponseMapper {
    public static TicketResponse toDTO(Ticket ticket){
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .forTeam(ticket.getForTeam())
                .fromTeam(ticket.getFromTeam())
                .ticketStatus(ticket.getTicketStatus())
                .createdAt(ticket.getCreatedAt())
                .fromUser(UserResponseMapper.toDTO(ticket.getFromUser()))
                .assignedUser(UserResponseMapper.toDTO(ticket.getAssignedUser()))
                .build();
    }
}
