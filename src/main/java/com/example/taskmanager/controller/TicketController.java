package com.example.taskmanager.controller;

import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.services.TicketService;
import com.example.taskmanager.dto.requests.UpdateTicketRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(){
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId){
        return ResponseEntity.ok(ticketService.getTicketById(ticketId));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByTeam(@PathVariable Long teamId){
        return ResponseEntity.ok(ticketService.getTicketByTeam(teamId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByUser(@PathVariable Long userId){
        return ResponseEntity.ok(ticketService.getTicketByUser(userId));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody CreateTicketRequest ticket){
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(ticket));
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> updateTicket(@PathVariable Long ticketId, @RequestBody UpdateTicketRequest ticket){
        return ResponseEntity.ok(ticketService.updateTicket(ticketId, ticket));
    }
}
