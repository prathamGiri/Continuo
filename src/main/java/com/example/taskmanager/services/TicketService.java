package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketAssignedUserRequest;
import com.example.taskmanager.dto.requests.UpdateTicketStatusRequest;
import com.example.taskmanager.dto.responses.TicketResponse;

import java.util.List;

public interface TicketService {
    List<TicketResponse> getAllTickets();
    TicketResponse getTicketById(Long ticketId);
    List<TicketResponse> getTicketByTeam(Long teamId);
    List<TicketResponse> getTicketByUser(Long userId);
    TicketResponse updateTicketAssignedUser(Long ticketId, UpdateTicketAssignedUserRequest ticket);
    TicketResponse updateTicketStatus(Long ticketId, UpdateTicketStatusRequest ticket);
    TicketResponse createTicket(CreateTicketRequest request);
}
