package com.example.taskmanager.security;

import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.TicketStatus;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.exceptions.ForbiddenActionException;
import com.example.taskmanager.exceptions.UserNotFoundException;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessPolicyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccessPolicy policy;

    private Team engineering;
    private Team support;
    private User sysAdmin;
    private User engAdmin;      // TeamAdmin of Engineering
    private User engMember;     // Member of Engineering (the assignee)
    private User supportMember; // Member of Support (the ticket creator)
    private User outsider;      // No team
    private Ticket ticket;      // raised by Support for Engineering, assigned to engMember

    @BeforeEach
    void setUp() {
        engineering = Team.builder().teamId(1L).teamName("Engineering").build();
        support = Team.builder().teamId(2L).teamName("Support").build();

        sysAdmin = user(1L, UserRole.SysAdmin, null);
        engAdmin = user(2L, UserRole.TeamAdmin, engineering);
        engMember = user(3L, UserRole.Member, engineering);
        supportMember = user(4L, UserRole.Member, support);
        outsider = user(5L, UserRole.Normal, null);

        ticket = Ticket.builder()
                .ticketId(100L)
                .forTeam(engineering)
                .fromTeam(support)
                .fromUser(supportMember)
                .assignedUser(engMember)
                .ticketStatus(TicketStatus.WorkInProgress)
                .build();
    }

    private static User user(Long id, UserRole role, Team team) {
        return User.builder().userId(id).userName("u" + id).emailId("u" + id + "@x.com").role(role).team(team).build();
    }

    private void actorIs(User user) {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
    }

    @Test
    void unknownActorIsRejected() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> policy.loadActor(42L));
    }

    @Nested
    class ViewTicket {
        @Test void sysAdminAllowed()        { actorIs(sysAdmin);      assertDoesNotThrow(() -> policy.checkCanViewTicket(1L, ticket)); }
        @Test void receivingTeamAllowed()   { actorIs(engMember);     assertDoesNotThrow(() -> policy.checkCanViewTicket(3L, ticket)); }
        @Test void raisingTeamAllowed()     { actorIs(supportMember); assertDoesNotThrow(() -> policy.checkCanViewTicket(4L, ticket)); }
        @Test void outsiderForbidden()      { actorIs(outsider);      assertThrows(ForbiddenActionException.class, () -> policy.checkCanViewTicket(5L, ticket)); }
    }

    @Nested
    class ViewTeamTickets {
        @Test void memberAllowed()          { actorIs(engMember);     assertDoesNotThrow(() -> policy.checkCanViewTeamTickets(3L, 1L)); }
        @Test void otherTeamForbidden()     { actorIs(supportMember); assertThrows(ForbiddenActionException.class, () -> policy.checkCanViewTeamTickets(4L, 1L)); }
        @Test void sysAdminAllowed()        { actorIs(sysAdmin);      assertDoesNotThrow(() -> policy.checkCanViewTeamTickets(1L, 1L)); }
    }

    @Nested
    class ViewUserTickets {
        @Test void selfAllowed()            { actorIs(engMember);     assertDoesNotThrow(() -> policy.checkCanViewUserTickets(3L, 3L)); }

        @Test
        void teamAdminOfUsersTeamAllowed() {
            actorIs(engAdmin);
            when(userRepository.findById(3L)).thenReturn(Optional.of(engMember));
            assertDoesNotThrow(() -> policy.checkCanViewUserTickets(2L, 3L));
        }

        @Test
        void teamAdminOfOtherTeamForbidden() {
            actorIs(engAdmin);
            when(userRepository.findById(4L)).thenReturn(Optional.of(supportMember));
            assertThrows(ForbiddenActionException.class, () -> policy.checkCanViewUserTickets(2L, 4L));
        }

        @Test void memberViewingOtherForbidden() { actorIs(supportMember); assertThrows(ForbiddenActionException.class, () -> policy.checkCanViewUserTickets(4L, 3L)); }
    }

    @Nested
    class EditTicket {
        @Test void creatorAllowed()                { actorIs(supportMember); assertDoesNotThrow(() -> policy.checkCanEditTicket(4L, ticket)); }
        @Test void receivingTeamAdminAllowed()     { actorIs(engAdmin);      assertDoesNotThrow(() -> policy.checkCanEditTicket(2L, ticket)); }
        @Test void plainMemberForbidden()          { actorIs(engMember);     assertThrows(ForbiddenActionException.class, () -> policy.checkCanEditTicket(3L, ticket)); }
    }

    @Nested
    class AssignTicket {
        @Test void receivingTeamAdminAllowed()     { actorIs(engAdmin);      assertDoesNotThrow(() -> policy.checkCanAssignTicket(2L, ticket)); }
        @Test void sysAdminAllowed()               { actorIs(sysAdmin);      assertDoesNotThrow(() -> policy.checkCanAssignTicket(1L, ticket)); }
        @Test void memberForbidden()               { actorIs(engMember);     assertThrows(ForbiddenActionException.class, () -> policy.checkCanAssignTicket(3L, ticket)); }
        @Test void creatorForbidden()              { actorIs(supportMember); assertThrows(ForbiddenActionException.class, () -> policy.checkCanAssignTicket(4L, ticket)); }

        @Test
        void teamAdminOfOtherTeamForbidden() {
            User supportAdmin = user(6L, UserRole.TeamAdmin, support);
            actorIs(supportAdmin);
            assertThrows(ForbiddenActionException.class, () -> policy.checkCanAssignTicket(6L, ticket));
        }
    }

    @Nested
    class ChangeStatus {
        @Test void assigneeCanMarkDone()           { actorIs(engMember); assertDoesNotThrow(() -> policy.checkCanChangeStatus(3L, ticket, TicketStatus.Done)); }
        @Test void teamAdminCannotMarkDone()       { actorIs(engAdmin);  assertThrows(ForbiddenActionException.class, () -> policy.checkCanChangeStatus(2L, ticket, TicketStatus.Done)); }
        @Test void sysAdminCannotMarkDone()        { actorIs(sysAdmin);  assertThrows(ForbiddenActionException.class, () -> policy.checkCanChangeStatus(1L, ticket, TicketStatus.Done)); }
        @Test void teamAdminCanSetPending()        { actorIs(engAdmin);  assertDoesNotThrow(() -> policy.checkCanChangeStatus(2L, ticket, TicketStatus.Pending)); }
        @Test void creatorCannotChangeStatus()     { actorIs(supportMember); assertThrows(ForbiddenActionException.class, () -> policy.checkCanChangeStatus(4L, ticket, TicketStatus.Pending)); }

        @Test
        void nobodyCanMarkUnassignedTicketDone() {
            ticket.setAssignedUser(null);
            actorIs(engAdmin);
            assertThrows(ForbiddenActionException.class, () -> policy.checkCanChangeStatus(2L, ticket, TicketStatus.Done));
        }
    }

    @Nested
    class TeamMembership {
        @Test void sysAdminCanAddAnyone()          { actorIs(sysAdmin); assertDoesNotThrow(() -> policy.checkCanAddToTeam(1L, supportMember, 1L)); }
        @Test void teamAdminCanAddTeamlessUser()   { actorIs(engAdmin); assertDoesNotThrow(() -> policy.checkCanAddToTeam(2L, outsider, 1L)); }
        @Test void teamAdminCannotPoachMembers()   { actorIs(engAdmin); assertThrows(ForbiddenActionException.class, () -> policy.checkCanAddToTeam(2L, supportMember, 1L)); }
        @Test void teamAdminCannotAddToOtherTeam() { actorIs(engAdmin); assertThrows(ForbiddenActionException.class, () -> policy.checkCanAddToTeam(2L, outsider, 2L)); }
        @Test void teamAdminCanRemoveOwnMember()   { actorIs(engAdmin); assertDoesNotThrow(() -> policy.checkCanManageTeamMembers(2L, 1L)); }
        @Test void teamAdminCannotRemoveOthers()   { actorIs(engAdmin); assertThrows(ForbiddenActionException.class, () -> policy.checkCanManageTeamMembers(2L, 2L)); }
        @Test void memberCannotRemove()            { actorIs(engMember); assertThrows(ForbiddenActionException.class, () -> policy.checkCanManageTeamMembers(3L, 1L)); }
    }
}
