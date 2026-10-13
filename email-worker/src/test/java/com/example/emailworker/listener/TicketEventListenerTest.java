package com.example.emailworker.listener;

import com.example.emailworker.mail.EmailComposer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import tools.jackson.core.JacksonException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketEventListenerTest {

    @Mock
    private MailSender mailSender;

    private TicketEventListener listener;

    // Same shape the backend publishes, plus an extra field to prove unknown fields are ignored.
    private static final String CREATED_EVENT = """
            {
              "eventType": "TICKET_CREATED",
              "ticketId": 7,
              "title": "VPN down",
              "description": null,
              "priority": "Critical",
              "status": "Created",
              "forTeam": "IT",
              "fromTeam": "Sales",
              "raisedBy": "dave",
              "assignedTo": null,
              "dueDate": "2026-11-01",
              "recipients": ["it1@x.com", "it2@x.com"],
              "occurredAt": "2026-10-14T10:15:30Z",
              "someFutureField": true
            }
            """;

    @BeforeEach
    void setUp() {
        listener = new TicketEventListener(new EmailComposer("noreply@x.com"), mailSender);
    }

    @Test
    void sendsOneEmailPerRecipient() {
        listener.onTicketEvent(CREATED_EVENT);

        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(sent.capture());
        assertThat(sent.getAllValues()).extracting(m -> m.getTo()[0]).containsExactly("it1@x.com", "it2@x.com");
        assertThat(sent.getValue().getSubject()).isEqualTo("[Ticket #7] New Critical priority ticket from Sales: VPN down");
    }

    @Test
    void malformedJsonThrowsSoItGoesToTheDeadLetterTopic() {
        assertThrows(JacksonException.class, () -> listener.onTicketEvent("{not json"));
        verifyNoInteractions(mailSender);
    }

    @Test
    void mailFailurePropagatesSoKafkaCanRetry() {
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(MailSendException.class, () -> listener.onTicketEvent(CREATED_EVENT));
    }
}
