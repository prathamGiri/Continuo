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
import com.example.taskmanager.exceptions.TeamNotFoundException;
import com.example.taskmanager.exceptions.TicketNotFoundException;
import com.example.taskmanager.exceptions.UserNotFoundException;
import com.example.taskmanager.mappers.TicketMapper;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.TicketRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService{
    private final TicketRepository ticketRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public List<TicketResponse> getAllTickets(){
        return ticketRepository.findAll()
                .stream()
                .map(TicketMapper::toDTO)
                .toList();
    }

    @Override
    public TicketResponse getTicketById(Long ticketId){
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        return TicketMapper.toDTO(ticket);
    }

    @Override
    public List<TicketResponse> getTicketByTeam(Long teamId){
        return ticketRepository.findByForTeam_TeamIdOrderByCreatedAt(teamId)
                .stream()
                .map(TicketMapper::toDTO)
                .toList();
    }

    @Override
    public List<TicketResponse> getTicketByUser(Long userId){
        return ticketRepository.findByAssignedUser_UserIdOrderByCreatedAt(userId)
                .stream()
                .map(TicketMapper::toDTO)
                .toList();
    }

    @Override
    public TicketResponse createTicket(CreateTicketRequest request){
        Team forTeam = teamRepository.findById(request.getForTeamId())
                .orElseThrow(() -> new TeamNotFoundException(request.getForTeamId()));
        Team fromTeam = teamRepository.findById(request.getFromTeamId())
                .orElseThrow(() -> new TeamNotFoundException(request.getFromTeamId()));
        User fromUser = userRepository.findById(request.getFromUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getFromUserId()));

        Ticket ticket = TicketMapper.toEntity(request, forTeam, fromTeam, fromUser);

        return TicketMapper.toDTO(ticketRepository.save(ticket));
    }

    @Override
    public TicketResponse updateTicketStatus(Long ticketId, UpdateTicketStatusRequest request){
        Ticket existing = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        TicketStatus newStatus = request.getTicketStatus();
        if (newStatus == TicketStatus.Done && existing.getTicketStatus() != TicketStatus.Done) {
            existing.setClosedAt(LocalDateTime.now());
        } else if (newStatus != TicketStatus.Done) {
            existing.setClosedAt(null);
        }
        existing.setTicketStatus(newStatus);
        return TicketMapper.toDTO(ticketRepository.save(existing));
    }

    @Override
    public TicketResponse updateTicketDetails(Long ticketId, UpdateTicketDetailsRequest request){
        Ticket existing = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
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
    public TicketResponse updateTicketAssignedUser(Long ticketId, UpdateTicketAssignedUserRequest ticket){
        Ticket existing = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        User assignedUser = userRepository.findById(ticket.getAssignedUserId())
                .orElseThrow(() -> new UserNotFoundException(ticket.getAssignedUserId()));
        existing.setAssignedUser(assignedUser);
        return TicketMapper.toDTO(ticketRepository.save(existing));
    }
}
