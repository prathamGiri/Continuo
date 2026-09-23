package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketRequest;
import com.example.taskmanager.dto.responses.TicketResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.mappers.TicketResponseMapper;
import com.example.taskmanager.mappers.UserResponseMapper;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.TicketRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
                .map(TicketResponseMapper::toDTO)
                .toList();
    };

    @Override
    public TicketResponse getTicketById(Long ticketId){
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));
        return TicketResponseMapper.toDTO(ticket);
    }

    @Override
    public List<TicketResponse> getTicketByTeam(Long teamId){
        return ticketRepository.findByForTeam_TeamIdOrderByCreatedAt(teamId)
                .stream()
                .map(TicketResponseMapper::toDTO)
                .toList();
    }

    @Override
    public List<TicketResponse> getTicketByUser(Long userId){
        return ticketRepository.findByAssignedUser_UserIdOrderByCreatedAt(userId)
                .stream()
                .map(TicketResponseMapper::toDTO)
                .toList();
    }

    @Override
    public TicketResponse createTicket(CreateTicketRequest request){
        Team forTeam = teamRepository.findById(request.getForTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        Team fromTeam = teamRepository.findById(request.getFromTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User fromUser = userRepository.findById(request.getFromUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = new Ticket();
        ticket.setForTeam(forTeam);
        ticket.setFromTeam(fromTeam);
        ticket.setFromUser(fromUser);
        ticket.setTicketStatus(request.getTicketStatus());

        return TicketResponseMapper.toDTO(ticketRepository.save(ticket));
    }

    @Override
    public TicketResponse updateTicket(Long ticketId, UpdateTicketRequest ticket){
        Ticket existing = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        User assignedUser;
        if (ticket.getAssignedUserId() != null){
            assignedUser = userRepository.findById(ticket.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            existing.setAssignedUser(assignedUser);
        }
        if (ticket.getTicketStatus() != null){
            existing.setTicketStatus(ticket.getTicketStatus());
        }
        return TicketResponseMapper.toDTO(ticketRepository.save(existing));
    }
}
