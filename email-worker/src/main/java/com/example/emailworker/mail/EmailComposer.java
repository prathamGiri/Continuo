package com.example.emailworker.mail;

import com.example.emailworker.event.TicketEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/** Turns a ticket event into one plain-text email per recipient. */
@Component
public class EmailComposer {

    private final String from;

    public EmailComposer(@Value("${app.mail.from}") String from) {
        this.from = from;
    }

    public List<SimpleMailMessage> compose(TicketEvent event) {
        if (event.recipients() == null || event.recipients().isEmpty()) {
            return List.of();
        }
        String subject = subject(event);
        String body = body(event);
        return event.recipients().stream()
                .map(to -> {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(from);
                    message.setTo(to);
                    message.setSubject(subject);
                    message.setText(body);
                    return message;
                })
                .toList();
    }

    String subject(TicketEvent event) {
        return switch (event.eventType()) {
            case TICKET_CREATED -> String.format("[Ticket #%d] New %s priority ticket from %s: %s",
                    event.ticketId(), orDash(event.priority()), orDash(event.fromTeam()), event.title());
            case TICKET_RESOLVED -> String.format("[Ticket #%d] Resolved: %s", event.ticketId(), event.title());
        };
    }

    String body(TicketEvent event) {
        String intro = switch (event.eventType()) {
            case TICKET_CREATED -> String.format("A new ticket has been raised for your team (%s).", orDash(event.forTeam()));
            case TICKET_RESOLVED -> String.format("A ticket raised by your team (%s) has been resolved by %s.",
                    orDash(event.fromTeam()), orDash(event.forTeam()));
        };
        return intro + "\n\n"
                + "Ticket:      #" + event.ticketId() + " - " + event.title() + "\n"
                + "Priority:    " + orDash(event.priority()) + "\n"
                + "Status:      " + orDash(event.status()) + "\n"
                + "Raised by:   " + orDash(event.raisedBy()) + " (" + orDash(event.fromTeam()) + ")\n"
                + "Assigned to: " + orDash(event.assignedTo()) + "\n"
                + "Due date:    " + (event.dueDate() == null ? "-" : event.dueDate()) + "\n"
                + (event.description() == null || event.description().isBlank()
                        ? "" : "\nDescription:\n" + event.description() + "\n")
                + "\n-- Task Manager";
    }

    private static String orDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
