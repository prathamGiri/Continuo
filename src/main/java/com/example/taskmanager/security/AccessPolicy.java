package com.example.taskmanager.security;

import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.TicketStatus;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.exceptions.ForbiddenActionException;
import com.example.taskmanager.exceptions.UserNotFoundException;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Authorization rules that depend on data (team membership, ticket ownership).
 * Pure role checks live on the controllers as @PreAuthorize.
 *
 * The caller is always re-loaded from the database, so a role or team change
 * takes effect immediately, even while an older token is still valid.
 */
@Component
@RequiredArgsConstructor
public class AccessPolicy {

    private final UserRepository userRepository;

    public User loadActor(Long actorId) {
        return userRepository.findById(actorId)
                .orElseThrow(() -> new UserNotFoundException(actorId));
    }

    /** SysAdmin, or a member of the team that raised or received the ticket. */
    public void checkCanViewTicket(Long actorId, Ticket ticket) {
        User actor = loadActor(actorId);
        if (isSysAdmin(actor) || isMemberOf(actor, ticket.getForTeam()) || isMemberOf(actor, ticket.getFromTeam())) {
            return;
        }
        throw new ForbiddenActionException("You can only view tickets raised by or for your team");
    }

    /** SysAdmin, or a member of that team. */
    public void checkCanViewTeamTickets(Long actorId, Long teamId) {
        User actor = loadActor(actorId);
        if (isSysAdmin(actor) || isMemberOf(actor, teamId)) {
            return;
        }
        throw new ForbiddenActionException("You can only view your own team's tickets");
    }

    /** The user themselves, a SysAdmin, or the TeamAdmin of the user's team. */
    public void checkCanViewUserTickets(Long actorId, Long userId) {
        User actor = loadActor(actorId);
        if (isSysAdmin(actor) || actor.getUserId().equals(userId)) {
            return;
        }
        if (actor.getRole() == UserRole.TeamAdmin) {
            User target = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            if (isMemberOf(actor, target.getTeam())) {
                return;
            }
        }
        throw new ForbiddenActionException("You can only view tickets assigned to you or to members of the team you administer");
    }

    /** The ticket's creator, the TeamAdmin of the receiving team, or a SysAdmin. */
    public void checkCanEditTicket(Long actorId, Ticket ticket) {
        User actor = loadActor(actorId);
        boolean isCreator = ticket.getFromUser() != null && actor.getUserId().equals(ticket.getFromUser().getUserId());
        if (isSysAdmin(actor) || isCreator || isTeamAdminOf(actor, ticket.getForTeam())) {
            return;
        }
        throw new ForbiddenActionException("Only the ticket's creator or the receiving team's TeamAdmin can edit it");
    }

    /** SysAdmin, or the TeamAdmin of the team the ticket was raised for. */
    public void checkCanAssignTicket(Long actorId, Ticket ticket) {
        User actor = loadActor(actorId);
        if (isSysAdmin(actor) || isTeamAdminOf(actor, ticket.getForTeam())) {
            return;
        }
        throw new ForbiddenActionException("Only the receiving team's TeamAdmin can assign this ticket");
    }

    /**
     * Only the assignee can mark a ticket Done.
     * Other status changes: the assignee, the receiving team's TeamAdmin, or a SysAdmin.
     */
    public void checkCanChangeStatus(Long actorId, Ticket ticket, TicketStatus newStatus) {
        User actor = loadActor(actorId);
        boolean isAssignee = ticket.getAssignedUser() != null
                && actor.getUserId().equals(ticket.getAssignedUser().getUserId());

        if (newStatus == TicketStatus.Done) {
            if (isAssignee) {
                return;
            }
            throw new ForbiddenActionException("Only the assigned user can mark a ticket as Done");
        }
        if (isAssignee || isSysAdmin(actor) || isTeamAdminOf(actor, ticket.getForTeam())) {
            return;
        }
        throw new ForbiddenActionException("Only the assignee or the receiving team's TeamAdmin can change this ticket's status");
    }

    /**
     * Adding a user to a team: a SysAdmin can move anyone; a TeamAdmin can only add
     * users who are not in any team yet, and only to their own team.
     */
    public void checkCanAddToTeam(Long actorId, User target, Long newTeamId) {
        User actor = loadActor(actorId);
        if (isSysAdmin(actor)) {
            return;
        }
        if (actor.getRole() == UserRole.TeamAdmin && isMemberOf(actor, newTeamId) && target.getTeam() == null) {
            return;
        }
        throw new ForbiddenActionException("A TeamAdmin can only add users without a team to their own team");
    }

    /** Removing someone from a team: a SysAdmin, or the TeamAdmin of that team. */
    public void checkCanManageTeamMembers(Long actorId, Long teamId) {
        User actor = loadActor(actorId);
        if (isSysAdmin(actor)) {
            return;
        }
        if (teamId != null && actor.getRole() == UserRole.TeamAdmin && isMemberOf(actor, teamId)) {
            return;
        }
        throw new ForbiddenActionException("Only a SysAdmin or that team's TeamAdmin can manage its members");
    }

    private static boolean isSysAdmin(User user) {
        return user.getRole() == UserRole.SysAdmin;
    }

    private static boolean isMemberOf(User user, Team team) {
        return team != null && isMemberOf(user, team.getTeamId());
    }

    private static boolean isMemberOf(User user, Long teamId) {
        return teamId != null && user.getTeam() != null && teamId.equals(user.getTeam().getTeamId());
    }

    private static boolean isTeamAdminOf(User user, Team team) {
        return user.getRole() == UserRole.TeamAdmin && isMemberOf(user, team);
    }
}
