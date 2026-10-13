package com.example.taskmanager.events;

import com.example.taskmanager.entities.Ticket;

/**
 * Announces ticket lifecycle events. Implementations must never throw:
 * a failed notification must not roll back or fail the ticket operation itself.
 */
public interface TicketEventPublisher {

    /** A new ticket was raised: notify the members of the receiving team. */
    void ticketCreated(Ticket ticket);

    /** A ticket was marked Done: notify the members of the team that raised it. */
    void ticketResolved(Ticket ticket);
}
