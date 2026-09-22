package com.example.taskmanager.repository;

import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByForTeam(Team team);
    List<Ticket> findByForTeam_TeamIdOrderByCreatedAt(Long teamId);
    List<Ticket> findByAssignedUser_UserIdOrderByCreatedAt(Long userId);
}
