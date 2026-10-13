package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketAssignedUserRequest;
import com.example.taskmanager.dto.requests.UpdateTicketDetailsRequest;
import com.example.taskmanager.dto.requests.UpdateTicketStatusRequest;
import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.TicketPriority;
import com.example.taskmanager.enums.TicketStatus;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.events.TicketEventPublisher;
import com.example.taskmanager.exceptions.ForbiddenActionException;
import com.example.taskmanager.exceptions.InvalidTicketAssignmentException;
import com.example.taskmanager.exceptions.NoTeamAssignedException;
import com.example.taskmanager.exceptions.TeamNotFoundException;
import com.example.taskmanager.exceptions.TicketNotFoundException;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.TicketRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.AccessPolicy;
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

    private static final Long ACTOR_ID = 10L;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    // Authorization rules are unit-tested in AccessPolicyTest; by default the mock allows everything.
    @Mock
    private AccessPolicy accessPolicy;

    @Mock
    private TicketEventPublisher eventPublisher;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Team engineering;
    private Team support;
    private User reporter;
    private User engineer;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        engineering = Team.builder().teamId(1L).teamName("Engineering").build();
        support = Team.builder().teamId(2L).teamName("Support").build();
        reporter = User.builder().userId(ACTOR_ID).userName("alice").emailId("alice@gmail.com")
                .role(UserRole.Member).team(support).build();
        engineer = User.builder().userId(20L).userName("bob").emailId("bob@gmail.com")
                .role(UserRole.Member).team(engineering).build();

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
        void takesCreatorAndTeamFromCallerAndPublishesEvent() {
            LocalDate due = LocalDate.now().plusDays(5);
            CreateTicketRequest request = new CreateTicketRequest(
                    "  Fix login  ", "Steps to reproduce...", TicketPriority.Critical, due, 1L);
            when(accessPolicy.loadActor(ACTOR_ID)).thenReturn(reporter);
            when(teamRepository.findById(1L)).thenReturn(Optional.of(engineering));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketResponse result = ticketService.createTicket(ACTOR_ID, request);

            ArgumentCaptor<Ticket> saved = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(saved.capture());
            assertThat(saved.getValue().getTitle()).isEqualTo("Fix login");
            assertThat(saved.getValue().getFromUser()).isSameAs(reporter);
            assertThat(saved.getValue().getFromTeam()).isSameAs(support);
            assertThat(result.getForTeam().getTeamId()).isEqualTo(1L);
            assertThat(result.getPriority()).isEqualTo(TicketPriority.Critical);
            assertThat(result.getDueDate()).isEqualTo(due);
            verify(eventPublisher).ticketCreated(saved.getValue());
        }

        @Test
        void rejectsCallerWithoutTeam() {
            reporter.setTeam(null);
            when(accessPolicy.loadActor(ACTOR_ID)).thenReturn(reporter);

            assertThrows(NoTeamAssignedException.class,
                    () -> ticketService.createTicket(ACTOR_ID, new CreateTicketRequest("T", null, null, null, 1L)));
            verify(ticketRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void throwsWhenTargetTeamMissing() {
            when(accessPolicy.loadActor(ACTOR_ID)).thenReturn(reporter);
            when(teamRepository.findById(99L)).thenReturn(Optional.empty());

            TeamNotFoundException ex = assertThrows(TeamNotFoundException.class,
                    () -> ticketService.createTicket(ACTOR_ID, new CreateTicketRequest("T", null, null, null, 99L)));
            assertThat(ex.getMessage()).contains("99");
            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    class GetTicket {

        @Test
        void checksViewPermission() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

            ticketService.getTicketById(ACTOR_ID, 100L);

            verify(accessPolicy).checkCanViewTicket(ACTOR_ID, ticket);
        }

        @Test
        void propagatesForbidden() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            doThrow(new ForbiddenActionException("no")).when(accessPolicy).checkCanViewTicket(ACTOR_ID, ticket);

            assertThrows(ForbiddenActionException.class, () -> ticketService.getTicketById(ACTOR_ID, 100L));
        }
    }

    @Nested
    class UpdateTicketDetails {

        @Test
        void updatesOnlyProvidedFields() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketResponse result = ticketService.updateTicketDetails(ACTOR_ID, 100L,
                    new UpdateTicketDetailsRequest(null, null, TicketPriority.Low, null));

            assertThat(result.getPriority()).isEqualTo(TicketPriority.Low);
            assertThat(result.getTitle()).isEqualTo("Login page broken");
            assertThat(result.getDescription()).isEqualTo("500 error on submit");
            verify(accessPolicy).checkCanEditTicket(ACTOR_ID, ticket);
        }

        @Test
        void throwsWhenTicketMissing() {
            when(ticketRepository.findById(5L)).thenReturn(Optional.empty());
            assertThrows(TicketNotFoundException.class,
                    () -> ticketService.updateTicketDetails(ACTOR_ID, 5L, new UpdateTicketDetailsRequest()));
        }

        @Test
        void doesNotSaveWhenForbidden() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            doThrow(new ForbiddenActionException("no")).when(accessPolicy).checkCanEditTicket(ACTOR_ID, ticket);

            assertThrows(ForbiddenActionException.class,
                    () -> ticketService.updateTicketDetails(ACTOR_ID, 100L, new UpdateTicketDetailsRequest()));
            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    class AssignTicket {

        @Test
        void assignsMemberOfReceivingTeam() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(20L)).thenReturn(Optional.of(engineer));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketResponse result = ticketService.updateTicketAssignedUser(ACTOR_ID, 100L,
                    new UpdateTicketAssignedUserRequest(20L));

            assertThat(result.getAssignedUser().getUserId()).isEqualTo(20L);
            verify(accessPolicy).checkCanAssignTicket(ACTOR_ID, ticket);
        }

        @Test
        void rejectsAssigneeFromAnotherTeam() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(ACTOR_ID)).thenReturn(Optional.of(reporter)); // reporter is in Support

            assertThrows(InvalidTicketAssignmentException.class,
                    () -> ticketService.updateTicketAssignedUser(ACTOR_ID, 100L,
                            new UpdateTicketAssignedUserRequest(ACTOR_ID)));
            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateTicketStatus {

        @Test
        void resolvingSetsClosedAtAndPublishesEvent() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketResponse result = ticketService.updateTicketStatus(ACTOR_ID, 100L,
                    new UpdateTicketStatusRequest(TicketStatus.Done));

            assertThat(result.getTicketStatus()).isEqualTo(TicketStatus.Done);
            assertThat(result.getClosedAt()).isNotNull();
            verify(accessPolicy).checkCanChangeStatus(ACTOR_ID, ticket, TicketStatus.Done);
            verify(eventPublisher).ticketResolved(ticket);
        }

        @Test
        void nonResolvingChangeDoesNotPublish() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            ticketService.updateTicketStatus(ACTOR_ID, 100L, new UpdateTicketStatusRequest(TicketStatus.WorkInProgress));

            verifyNoInteractions(eventPublisher);
        }

        @Test
        void markingDoneTwiceDoesNotPublishAgain() {
            ticket.setTicketStatus(TicketStatus.Done);
            ticket.setClosedAt(LocalDateTime.now().minusDays(1));
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            ticketService.updateTicketStatus(ACTOR_ID, 100L, new UpdateTicketStatusRequest(TicketStatus.Done));

            verifyNoInteractions(eventPublisher);
        }

        @Test
        void clearsClosedAtWhenReopened() {
            ticket.setTicketStatus(TicketStatus.Done);
            ticket.setClosedAt(LocalDateTime.now().minusDays(1));
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketResponse result = ticketService.updateTicketStatus(ACTOR_ID, 100L,
                    new UpdateTicketStatusRequest(TicketStatus.WorkInProgress));

            assertThat(result.getClosedAt()).isNull();
        }

        @Test
        void doesNotChangeAnythingWhenForbidden() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            doThrow(new ForbiddenActionException("no"))
                    .when(accessPolicy).checkCanChangeStatus(ACTOR_ID, ticket, TicketStatus.Done);

            assertThrows(ForbiddenActionException.class, () -> ticketService.updateTicketStatus(ACTOR_ID, 100L,
                    new UpdateTicketStatusRequest(TicketStatus.Done)));
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.Created);
            verify(ticketRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    class Overdue {

        @Test
        void openTicketPastDueDateIsOverdue() {
            ticket.setDueDate(LocalDate.now().minusDays(1));
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

            assertThat(ticketService.getTicketById(ACTOR_ID, 100L).isOverdue()).isTrue();
        }

        @Test
        void doneTicketIsNeverOverdue() {
            ticket.setDueDate(LocalDate.now().minusDays(1));
            ticket.setTicketStatus(TicketStatus.Done);
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

            assertThat(ticketService.getTicketById(ACTOR_ID, 100L).isOverdue()).isFalse();
        }
    }
}
