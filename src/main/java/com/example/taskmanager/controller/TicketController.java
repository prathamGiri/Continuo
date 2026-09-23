package com.example.taskmanager.controller;

import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.services.TicketService;
import com.example.taskmanager.dto.requests.UpdateTicketRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    @GetMapping
    public List<TicketResponse> getAllTickets(){
        return ticketService.getAllTickets();
    }

    @GetMapping("/{ticketId}")
    public TicketResponse getTicketById(@PathVariable Long ticketId){
        return ticketService.getTicketById(ticketId);
    }

    @GetMapping("/team/{teamId}")
    public List<TicketResponse> getTicketsByTeam(@PathVariable Long teamId){
        return ticketService.getTicketByTeam(teamId);
    }

    @GetMapping("/user/{userId}")
    public List<TicketResponse> getTicketsByUser(@PathVariable Long userId){
        return ticketService.getTicketByUser(userId);
    }

    @PostMapping
    public TicketResponse createTicket(@RequestBody CreateTicketRequest ticket){
        return ticketService.createTicket(ticket);
    }

    @PatchMapping("/{ticketId}")
    public TicketResponse updateTicket(@PathVariable Long ticketId, @RequestBody UpdateTicketRequest ticket){
        return ticketService.updateTicket(ticketId, ticket);
    }
}
