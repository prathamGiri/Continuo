package com.example.taskmanager.controller;

import com.example.taskmanager.dto.requests.UpdateTicketAssignedUserRequest;
import com.example.taskmanager.dto.requests.UpdateTicketDetailsRequest;
import com.example.taskmanager.dto.requests.UpdateTicketStatusRequest;
import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest ticket){
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(ticket));
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> updateTicketDetails(@PathVariable Long ticketId, @Valid @RequestBody UpdateTicketDetailsRequest request){
        return ResponseEntity.ok(ticketService.updateTicketDetails(ticketId, request));
    }

    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponse> updateTicketAssignedUser(@PathVariable Long ticketId, @Valid @RequestBody UpdateTicketAssignedUserRequest request){
        return ResponseEntity.ok(ticketService.updateTicketAssignedUser(ticketId, request));
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(@PathVariable Long ticketId, @Valid @RequestBody UpdateTicketStatusRequest request){
        return ResponseEntity.ok(ticketService.updateTicketStatus(ticketId, request));
    }
}
