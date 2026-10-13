package com.example.taskmanager.events;

import com.example.taskmanager.entities.Ticket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Used when app.notifications.enabled=false (the default), so the API runs
 * locally without a Kafka broker.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.notifications.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingTicketEventPublisher implements TicketEventPublisher {

    @Override
    public void ticketCreated(Ticket ticket) {
        log.info("Notifications disabled: would announce creation of ticket {}", ticket.getTicketId());
    }

    @Override
    public void ticketResolved(Ticket ticket) {
        log.info("Notifications disabled: would announce resolution of ticket {}", ticket.getTicketId());
    }
}
