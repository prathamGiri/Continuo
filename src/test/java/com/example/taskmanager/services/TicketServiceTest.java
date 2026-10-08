package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketDetailsRequest;
import com.example.taskmanager.dto.requests.UpdateTicketStatusRequest;
import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.TicketPriority;
import com.example.taskmanager.enums.TicketStatus;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.exceptions.TeamNotFoundException;
import com.example.taskmanager.exceptions.TicketNotFoundException;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.TicketRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Team engineering;
    private Team support;
    private User reporter;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        engineering = Team.builder().teamId(1L).teamName("Engineering").build();
        support = Team.builder().teamId(2L).teamName("Support").build();
        reporter = User.builder().userId(10L).userName("alice").emailId("alice@gmail.com")
                .role(UserRole.Member).team(support).build();

        ticket = Ticket.builder()
                .ticketId(100L)
                .title("Login page broken")
                .description("500 error on submit")
                .priority(TicketPriority.High)
                .dueDate(LocalDate.now().plusDays(3))
                .forTeam(engineering)
                .fromTeam(support)
                .fromUser(reporter)
                .ticketStatus(TicketStatus.Created)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    class CreateTicket {

        @Test
        void mapsAllDetailsOntoTheSavedTicket() {
            LocalDate due = LocalDate.now().plusDays(5);
            CreateTicketRequest request = new CreateTicketRequest(
                    "  Fix login  ", "Steps to reproduce...", TicketPriority.Critical, due, 1L, 2L, 10L);
            when(teamRepository.findById(1L)).thenReturn(Optional.of(engineering));
            when(teamRepository.findById(2L)).thenReturn(Optional.of(support));
            when(userRepository.findById(10L)).thenReturn(Optional.of(reporter));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketResponse result = ticketService.createTicket(request);

            ArgumentCaptor<Ticket> saved = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(saved.capture());
            assertThat(saved.getValue().getTitle()).isEqualTo("Fix login");
            assertThat(saved.getValue().getPriority()).isEqualTo(TicketPriority.Critical);
            assertThat(result.getDescription()).isEqualTo("Steps to reproduce...");
            assertThat(result.getDueDate()).isEqualTo(due);
            assertThat(result.getForTeam().getTeamId()).isEqualTo(1L);
            assertThat(result.getFromUser().getUserId()).isEqualTo(10L);
        }

        @Test
        void reportsTheMissingFromTeamId() {
            CreateTicketRequest request = new CreateTicketRequest(
                    "Title", null, null, null, 1L, 99L, 10L);
            when(teamRepository.findById(1L)).thenReturn(Optional.of(engineering));
            when(teamRepository.findById(99L)).thenReturn(Optional.empty());

            TeamNotFoundException ex = assertThrows(TeamNotFoundException.class,
                    () -> ticketService.createTicket(request));
            assertThat(ex.getMessage()).contains("99");
            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateTicketDetails {

        @Test
        void updatesOnlyProvidedFields() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateTicketDetailsRequest request = new UpdateTicketDetailsRequest(
                    null, null, TicketPriority.Low, null);
            TicketResponse result = ticketService.updateTicketDetails(100L, request);

            assertThat(result.getPriority()).isEqualTo(TicketPriority.Low);
            assertThat(result.getTitle()).isEqualTo("Login page broken");
            assertThat(result.getDescription()).isEqualTo("500 error on submit");
        }

        @Test
        void throwsWhenTicketMissing() {
            when(ticketRepository.findById(5L)).thenReturn(Optional.empty());
            assertThrows(TicketNotFoundException.class,
                    () -> ticketService.updateTicketDetails(5L, new UpdateTicketDetailsRequest()));
        }
    }

    @Nested
    class UpdateTicketStatus {

        @Test
        void setsClosedAtWhenMarkedDone() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketResponse result = ticketService.updateTicketStatus(100L,
                    new UpdateTicketStatusRequest(TicketStatus.Done));

            assertThat(result.getTicketStatus()).isEqualTo(TicketStatus.Done);
            assertThat(result.getClosedAt()).isNotNull();
        }

        @Test
        void clearsClosedAtWhenReopened() {
            ticket.setTicketStatus(TicketStatus.Done);
            ticket.setClosedAt(LocalDateTime.now().minusDays(1));
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketResponse result = ticketService.updateTicketStatus(100L,
                    new UpdateTicketStatusRequest(TicketStatus.WorkInProgress));

            assertThat(result.getClosedAt()).isNull();
        }
    }

    @Nested
    class Overdue {

        @Test
        void openTicketPastDueDateIsOverdue() {
            ticket.setDueDate(LocalDate.now().minusDays(1));
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

            assertThat(ticketService.getTicketById(100L).isOverdue()).isTrue();
        }

        @Test
        void doneTicketIsNeverOverdue() {
            ticket.setDueDate(LocalDate.now().minusDays(1));
            ticket.setTicketStatus(TicketStatus.Done);
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

            assertThat(ticketService.getTicketById(100L).isOverdue()).isFalse();
        }
    }
}
