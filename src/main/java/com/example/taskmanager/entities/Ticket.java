package com.example.taskmanager.entities;

import com.example.taskmanager.enums.TicketPriority;
import com.example.taskmanager.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    private LocalDate dueDate;

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

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Set when the ticket moves to Done; cleared if it is reopened.
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.ticketStatus = TicketStatus.Created;
        if (this.priority == null) {
            this.priority = TicketPriority.Medium;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
