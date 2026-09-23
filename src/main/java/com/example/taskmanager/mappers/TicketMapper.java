package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.entities.Ticket;

public class TicketMapper {
    public static TicketResponse toDTO(Ticket ticket){
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .forTeam(ticket.getForTeam())
                .fromTeam(ticket.getFromTeam())
                .ticketStatus(ticket.getTicketStatus())
                .createdAt(ticket.getCreatedAt())
                .fromUser(UserMapper.toDTO(ticket.getFromUser()))
                .assignedUser(UserMapper.toDTO(ticket.getAssignedUser()))
                .build();
    }
}
