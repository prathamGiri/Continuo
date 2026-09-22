package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketRequest;
import com.example.taskmanager.entities.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketService {
    List<Ticket> getAllTickets();
    Optional<Ticket> getTicketById(Long ticketId);
    List<Ticket> getTicketByTeam(Long teamId);
    List<Ticket> getTicketByUser(Long userId);
    Ticket updateTicket(Long ticketId, UpdateTicketRequest ticket);
    Ticket createTicket(CreateTicketRequest request);
}
