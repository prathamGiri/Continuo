package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.Ticket;

public class TicketMapper {
    public static TicketResponse toDTO(Ticket ticket){
        UserResponse forUser = ticket.getAssignedUser() == null ? null : UserMapper.toDTOWithoutTeam(ticket.getAssignedUser());
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .forTeam(TeamMapper.teamDTO(ticket.getForTeam()))
                .fromTeam(TeamMapper.teamDTO(ticket.getFromTeam()))
                .ticketStatus(ticket.getTicketStatus())
                .createdAt(ticket.getCreatedAt())
                .fromUser(UserMapper.toDTOWithoutTeam(ticket.getFromUser()))
                .assignedUser(forUser)
                .build();
    }
}
