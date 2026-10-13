package com.example.taskmanager.controller;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketAssignedUserRequest;
import com.example.taskmanager.dto.requests.UpdateTicketDetailsRequest;
import com.example.taskmanager.dto.requests.UpdateTicketStatusRequest;
import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.security.CurrentUser;
import com.example.taskmanager.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    @GetMapping
    @PreAuthorize("hasRole('SysAdmin')")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicketById(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getTicketById(CurrentUser.id(jwt), ticketId));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByTeam(@AuthenticationPrincipal Jwt jwt,
                                                                 @PathVariable Long teamId) {
        return ResponseEntity.ok(ticketService.getTicketByTeam(CurrentUser.id(jwt), teamId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByUser(@AuthenticationPrincipal Jwt jwt,
                                                                 @PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketByUser(CurrentUser.id(jwt), userId));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@AuthenticationPrincipal Jwt jwt,
                                                       @Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.createTicket(CurrentUser.id(jwt), request));
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> updateTicketDetails(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable Long ticketId,
                                                              @Valid @RequestBody UpdateTicketDetailsRequest request) {
        return ResponseEntity.ok(ticketService.updateTicketDetails(CurrentUser.id(jwt), ticketId, request));
    }

    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponse> updateTicketAssignedUser(@AuthenticationPrincipal Jwt jwt,
                                                                   @PathVariable Long ticketId,
                                                                   @Valid @RequestBody UpdateTicketAssignedUserRequest request) {
        return ResponseEntity.ok(ticketService.updateTicketAssignedUser(CurrentUser.id(jwt), ticketId, request));
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(@AuthenticationPrincipal Jwt jwt,
                                                             @PathVariable Long ticketId,
                                                             @Valid @RequestBody UpdateTicketStatusRequest request) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(CurrentUser.id(jwt), ticketId, request));
    }
}
