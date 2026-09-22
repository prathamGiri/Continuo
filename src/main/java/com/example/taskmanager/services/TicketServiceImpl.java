package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTicketRequest;
import com.example.taskmanager.dto.requests.UpdateTicketRequest;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.TicketRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketServiceImpl implements TicketService{
    private final TicketRepository ticketRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TicketServiceImpl(
            TicketRepository ticketRepository,
            TeamRepository teamRepository,
            UserRepository userRepository
    ){
        this.ticketRepository = ticketRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Ticket> getAllTickets(){
        return ticketRepository.findAll();
    };

    @Override
    public Optional<Ticket> getTicketById(Long ticketId){
        return ticketRepository.findById(ticketId);
    }

    @Override
    public List<Ticket> getTicketByTeam(Long teamId){
        return ticketRepository.findByForTeam_TeamIdOrderByCreatedAt(teamId);
    }

    @Override
    public List<Ticket> getTicketByUser(Long userId){
        return ticketRepository.findByAssignedUser_UserIdOrderByCreatedAt(userId);
    }

    @Override
    public Ticket createTicket(CreateTicketRequest request){
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

        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket updateTicket(Long ticketId, UpdateTicketRequest ticket){
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
        return ticketRepository.save(existing);
    }
}
