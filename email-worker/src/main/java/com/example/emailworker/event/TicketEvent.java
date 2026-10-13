package com.example.emailworker.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * JSON contract published by the taskmanager backend on the ticket-events topic
 * (mirror of com.example.taskmanager.events.TicketNotificationEvent).
 * Unknown fields are ignored, so the backend can add fields without breaking the worker.
 */
public record TicketEvent(
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
}
