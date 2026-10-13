package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketAssignedUserRequest;
import com.example.taskmanager.dto.requests.UpdateTicketDetailsRequest;
import com.example.taskmanager.dto.requests.UpdateTicketStatusRequest;
import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.TicketStatus;
import com.example.taskmanager.events.TicketEventPublisher;
import com.example.taskmanager.exceptions.InvalidTicketAssignmentException;
import com.example.taskmanager.exceptions.NoTeamAssignedException;
import com.example.taskmanager.exceptions.TeamNotFoundException;
import com.example.taskmanager.exceptions.TicketNotFoundException;
import com.example.taskmanager.exceptions.UserNotFoundException;
import com.example.taskmanager.mappers.TicketMapper;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.TicketRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.AccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final AccessPolicy accessPolicy;
    private final TicketEventPublisher eventPublisher;

    @Override
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(TicketMapper::toDTO)
                .toList();
    }

    @Override
    public TicketResponse getTicketById(Long actorId, Long ticketId) {
        Ticket ticket = findTicket(ticketId);
        accessPolicy.checkCanViewTicket(actorId, ticket);
        return TicketMapper.toDTO(ticket);
    }

    @Override
    public List<TicketResponse> getTicketByTeam(Long actorId, Long teamId) {
        accessPolicy.checkCanViewTeamTickets(actorId, teamId);
        return ticketRepository.findByForTeam_TeamIdOrderByCreatedAt(teamId)
                .stream()
                .map(TicketMapper::toDTO)
                .toList();
    }

    @Override
    public List<TicketResponse> getTicketByUser(Long actorId, Long userId) {
        accessPolicy.checkCanViewUserTickets(actorId, userId);
        return ticketRepository.findByAssignedUser_UserIdOrderByCreatedAt(userId)
                .stream()
                .map(TicketMapper::toDTO)
                .toList();
    }

    @Override
    public TicketResponse createTicket(Long actorId, CreateTicketRequest request) {
        // The creator and their team come from the token, not the request body.
        User creator = accessPolicy.loadActor(actorId);
        Team fromTeam = creator.getTeam();
        if (fromTeam == null) {
            throw new NoTeamAssignedException();
        }
        Team forTeam = teamRepository.findById(request.getForTeamId())
                .orElseThrow(() -> new TeamNotFoundException(request.getForTeamId()));

        Ticket saved = ticketRepository.save(TicketMapper.toEntity(request, forTeam, fromTeam, creator));
        eventPublisher.ticketCreated(saved);
        return TicketMapper.toDTO(saved);
    }

    @Override
    public TicketResponse updateTicketStatus(Long actorId, Long ticketId, UpdateTicketStatusRequest request) {
        Ticket existing = findTicket(ticketId);
        TicketStatus newStatus = request.getTicketStatus();
        accessPolicy.checkCanChangeStatus(actorId, existing, newStatus);

        boolean resolving = newStatus == TicketStatus.Done && existing.getTicketStatus() != TicketStatus.Done;
        if (resolving) {
            existing.setClosedAt(LocalDateTime.now());
        } else if (newStatus != TicketStatus.Done) {
            existing.setClosedAt(null);
        }
        existing.setTicketStatus(newStatus);

        Ticket saved = ticketRepository.save(existing);
        if (resolving) {
            eventPublisher.ticketResolved(saved);
        }
        return TicketMapper.toDTO(saved);
    }

    @Override
    public TicketResponse updateTicketDetails(Long actorId, Long ticketId, UpdateTicketDetailsRequest request) {
        Ticket existing = findTicket(ticketId);
        accessPolicy.checkCanEditTicket(actorId, existing);

        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle().strip());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            existing.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            existing.setDueDate(request.getDueDate());
        }
        return TicketMapper.toDTO(ticketRepository.save(existing));
    }

    @Override
    public TicketResponse updateTicketAssignedUser(Long actorId, Long ticketId, UpdateTicketAssignedUserRequest request) {
        Ticket existing = findTicket(ticketId);
        accessPolicy.checkCanAssignTicket(actorId, existing);

        User assignee = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getAssignedUserId()));
        Team forTeam = existing.getForTeam();
        if (assignee.getTeam() == null || !assignee.getTeam().getTeamId().equals(forTeam.getTeamId())) {
            throw new InvalidTicketAssignmentException(
                    "Tickets can only be assigned to members of team " + forTeam.getTeamName());
        }
        existing.setAssignedUser(assignee);
        return TicketMapper.toDTO(ticketRepository.save(existing));
    }

    private Ticket findTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }
}
