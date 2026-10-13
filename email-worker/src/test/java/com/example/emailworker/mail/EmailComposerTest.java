package com.example.emailworker.mail;

import com.example.emailworker.event.TicketEvent;
import com.example.emailworker.event.TicketEventType;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmailComposerTest {

    private final EmailComposer composer = new EmailComposer("noreply@taskmanager.dev");

    private static TicketEvent event(TicketEventType type, List<String> recipients) {
        return new TicketEvent(type, 12L, "Login page broken", "500 on submit", "High", "Created",
                "Engineering", "Support", "alice", null, LocalDate.of(2026, 10, 30), recipients, Instant.now());
    }

    @Test
    void createsOneMessagePerRecipient() {
        List<SimpleMailMessage> messages = composer.compose(
                event(TicketEventType.TICKET_CREATED, List.of("bob@x.com", "carol@x.com")));

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getTo()).containsExactly("bob@x.com");
        assertThat(messages.get(1).getTo()).containsExactly("carol@x.com");
        assertThat(messages.get(0).getFrom()).isEqualTo("noreply@taskmanager.dev");
    }

    @Test
    void createdSubjectMentionsPriorityAndRaisingTeam() {
        SimpleMailMessage message = composer.compose(event(TicketEventType.TICKET_CREATED, List.of("bob@x.com"))).get(0);

        assertThat(message.getSubject()).isEqualTo("[Ticket #12] New High priority ticket from Support: Login page broken");
        assertThat(message.getText())
                .contains("raised for your team (Engineering)")
                .contains("Assigned to: -")
                .contains("2026-10-30")
                .contains("500 on submit");
    }

    @Test
    void resolvedSubjectSaysResolved() {
        SimpleMailMessage message = composer.compose(event(TicketEventType.TICKET_RESOLVED, List.of("alice@x.com"))).get(0);

        assertThat(message.getSubject()).isEqualTo("[Ticket #12] Resolved: Login page broken");
        assertThat(message.getText()).contains("resolved by Engineering");
    }

    @Test
    void noRecipientsMeansNoMessages() {
        assertThat(composer.compose(event(TicketEventType.TICKET_CREATED, List.of()))).isEmpty();
        assertThat(composer.compose(event(TicketEventType.TICKET_CREATED, null))).isEmpty();
    }
}
