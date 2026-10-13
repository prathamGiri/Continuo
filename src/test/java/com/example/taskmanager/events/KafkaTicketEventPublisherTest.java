package com.example.taskmanager.events;

import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.TicketPriority;
import com.example.taskmanager.enums.TicketStatus;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaTicketEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private UserRepository userRepository;

    private KafkaTicketEventPublisher publisher;
    private Team engineering;
    private Team support;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        publisher = new KafkaTicketEventPublisher(kafkaTemplate, userRepository, JsonMapper.builder().build(), "ticket-events");

        engineering = Team.builder().teamId(1L).teamName("Engineering").build();
        support = Team.builder().teamId(2L).teamName("Support").build();
        User alice = User.builder().userId(10L).userName("alice").emailId("alice@x.com").team(support).build();
        ticket = Ticket.builder()
                .ticketId(100L).title("Login broken").priority(TicketPriority.High)
                .ticketStatus(TicketStatus.Created)
                .forTeam(engineering).fromTeam(support).fromUser(alice)
                .build();
    }

    @Test
    void createdEventGoesToReceivingTeamKeyedByTicketId() {
        when(userRepository.findByTeam(engineering)).thenReturn(List.of(
                User.builder().emailId("bob@x.com").build(),
                User.builder().emailId("carol@x.com").build()));
        when(kafkaTemplate.send(eq("ticket-events"), eq("100"), anyString())).thenReturn(new CompletableFuture<>());

        publisher.ticketCreated(ticket);

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("ticket-events"), eq("100"), payload.capture());
        assertThat(payload.getValue())
                .contains("\"eventType\":\"TICKET_CREATED\"")
                .contains("\"title\":\"Login broken\"")
                .contains("bob@x.com", "carol@x.com")
                .contains("\"forTeam\":\"Engineering\"");
    }

    @Test
    void resolvedEventGoesToRaisingTeam() {
        ticket.setTicketStatus(TicketStatus.Done);
        when(userRepository.findByTeam(support)).thenReturn(List.of(User.builder().emailId("alice@x.com").build()));
        when(kafkaTemplate.send(eq("ticket-events"), eq("100"), anyString())).thenReturn(new CompletableFuture<>());

        publisher.ticketResolved(ticket);

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("ticket-events"), eq("100"), payload.capture());
        assertThat(payload.getValue()).contains("\"eventType\":\"TICKET_RESOLVED\"").contains("alice@x.com");
    }

    @Test
    void skipsWhenTeamHasNoMembers() {
        when(userRepository.findByTeam(engineering)).thenReturn(List.of());

        publisher.ticketCreated(ticket);

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void neverThrowsWhenKafkaFails() {
        when(userRepository.findByTeam(engineering)).thenReturn(List.of(User.builder().emailId("bob@x.com").build()));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("broker down"));

        publisher.ticketCreated(ticket); // must not throw
    }
}
