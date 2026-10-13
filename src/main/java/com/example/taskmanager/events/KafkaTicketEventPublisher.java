package com.example.taskmanager.events;

import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Publishes ticket events as JSON to the "ticket-events" topic.
 * The ticket id is the message key, so all events for one ticket land on the same
 * partition and are consumed in order.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.notifications.enabled", havingValue = "true")
public class KafkaTicketEventPublisher implements TicketEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaTicketEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                     UserRepository userRepository,
                                     ObjectMapper objectMapper,
                                     @Value("${app.notifications.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void ticketCreated(Ticket ticket) {
        publish(TicketEventType.TICKET_CREATED, ticket, ticket.getForTeam());
    }

    @Override
    public void ticketResolved(Ticket ticket) {
        publish(TicketEventType.TICKET_RESOLVED, ticket, ticket.getFromTeam());
    }

    private void publish(TicketEventType type, Ticket ticket, Team audience) {
        try {
            List<String> recipients = audience == null ? List.of() :
                    userRepository.findByTeam(audience).stream().map(User::getEmailId).toList();
            if (recipients.isEmpty()) {
                log.info("No recipients for {} on ticket {}, skipping", type, ticket.getTicketId());
                return;
            }

            String payload = objectMapper.writeValueAsString(TicketNotificationEvent.of(type, ticket, recipients));
            String key = String.valueOf(ticket.getTicketId());

            kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish {} for ticket {}", type, ticket.getTicketId(), ex);
                } else {
                    log.info("Published {} for ticket {} to {}", type, ticket.getTicketId(), topic);
                }
            });
        } catch (RuntimeException ex) {
            // Never let a notification problem fail the ticket request itself.
            log.error("Could not publish {} for ticket {}", type, ticket.getTicketId(), ex);
        }
    }
}
