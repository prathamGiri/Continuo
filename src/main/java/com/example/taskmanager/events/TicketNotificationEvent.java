package com.example.taskmanager.events;

import com.example.taskmanager.entities.Ticket;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Message published to Kafka for the email worker.
 * It carries everything needed to write the email, including the recipients,
 * so the worker never needs access to this service's database.
 */
public record TicketNotificationEvent(
        TicketEventType eventType,
        Long ticketId,
        String title,
        String description,
        String priority,
        String status,
        String forTeam,
        String fromTeam,
        String raisedBy,
        String assignedTo,
        LocalDate dueDate,
        List<String> recipients,
        Instant occurredAt
) {
    public static TicketNotificationEvent of(TicketEventType type, Ticket ticket, List<String> recipients) {
        return new TicketNotificationEvent(
                type,
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority() == null ? null : ticket.getPriority().name(),
                ticket.getTicketStatus() == null ? null : ticket.getTicketStatus().name(),
                ticket.getForTeam() == null ? null : ticket.getForTeam().getTeamName(),
                ticket.getFromTeam() == null ? null : ticket.getFromTeam().getTeamName(),
                ticket.getFromUser() == null ? null : ticket.getFromUser().getUserName(),
                ticket.getAssignedUser() == null ? null : ticket.getAssignedUser().getUserName(),
                ticket.getDueDate(),
                List.copyOf(recipients),
                Instant.now()
        );
    }
}
