package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketRequest;
import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.entities.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketService {
    List<TicketResponse> getAllTickets();
    TicketResponse getTicketById(Long ticketId);
    List<TicketResponse> getTicketByTeam(Long teamId);
    List<TicketResponse> getTicketByUser(Long userId);
    TicketResponse updateTicket(Long ticketId, UpdateTicketRequest ticket);
    TicketResponse createTicket(CreateTicketRequest request);
}
