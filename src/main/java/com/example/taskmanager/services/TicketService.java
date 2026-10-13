package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketAssignedUserRequest;
import com.example.taskmanager.dto.requests.UpdateTicketDetailsRequest;
import com.example.taskmanager.dto.requests.UpdateTicketStatusRequest;
import com.example.taskmanager.dto.responses.TicketResponse;

import java.util.List;

/** actorId is the id of the authenticated caller, taken from the JWT. */
public interface TicketService {
    List<TicketResponse> getAllTickets();
    TicketResponse getTicketById(Long actorId, Long ticketId);
    List<TicketResponse> getTicketByTeam(Long actorId, Long teamId);
    List<TicketResponse> getTicketByUser(Long actorId, Long userId);
    TicketResponse createTicket(Long actorId, CreateTicketRequest request);
    TicketResponse updateTicketDetails(Long actorId, Long ticketId, UpdateTicketDetailsRequest request);
    TicketResponse updateTicketAssignedUser(Long actorId, Long ticketId, UpdateTicketAssignedUserRequest request);
    TicketResponse updateTicketStatus(Long actorId, Long ticketId, UpdateTicketStatusRequest request);
}
