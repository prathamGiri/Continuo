package com.example.taskmanager.dto.requests;

import com.example.taskmanager.enums.TicketPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotBlank(message = "Must Provide a Title")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    // Optional: defaults to Medium when not provided.
    private TicketPriority priority;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @NotNull(message = "Must Provide a Team")
    private Long forTeamId;
    @NotNull(message = "Must Provide a Team")
    private Long fromTeamId;
    @NotNull(message = "Must Provide a User")
    private Long fromUserId;
}
