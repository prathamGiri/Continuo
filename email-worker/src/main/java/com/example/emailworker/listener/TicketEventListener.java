package com.example.emailworker.listener;

import com.example.emailworker.event.TicketEvent;
import com.example.emailworker.mail.EmailComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Consumes ticket events and sends the notification emails.
 * If sending fails, the exception goes to the Kafka error handler, which retries
 * and finally parks the message on the dead-letter topic (see KafkaErrorHandlingConfig).
 */
@Component
public class TicketEventListener {

    private static final Logger log = LoggerFactory.getLogger(TicketEventListener.class);

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    private final EmailComposer composer;
    private final MailSender mailSender;

    public TicketEventListener(EmailComposer composer, MailSender mailSender) {
        this.composer = composer;
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = "${app.kafka.topic}")
    public void onTicketEvent(String payload) {
        TicketEvent event = objectMapper.readValue(payload, TicketEvent.class);

        List<SimpleMailMessage> messages = composer.compose(event);
        if (messages.isEmpty()) {
            log.info("{} for ticket {} has no recipients, nothing to send", event.eventType(), event.ticketId());
            return;
        }
        for (SimpleMailMessage message : messages) {
            mailSender.send(message);
        }
        log.info("Sent {} email(s) for {} on ticket {}", messages.size(), event.eventType(), event.ticketId());
    }
}
