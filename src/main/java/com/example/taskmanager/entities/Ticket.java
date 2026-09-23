package com.example.taskmanager.entities;

import com.example.taskmanager.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne
    @JoinColumn(name = "forTeamId", nullable = false)
    private Team forTeam;

    @ManyToOne
    @JoinColumn(name = "fromTeamId", nullable = false, updatable = false)
    private Team fromTeam;

    @ManyToOne
    @JoinColumn(name = "fromUserId", nullable = false, updatable = false)
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "assignedUserId")
    private User assignedUser;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
