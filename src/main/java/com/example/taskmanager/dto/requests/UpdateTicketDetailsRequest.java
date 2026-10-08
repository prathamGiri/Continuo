package com.example.taskmanager.dto.requests;

import com.example.taskmanager.enums.TicketPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Partial update: only the fields that are present (non-null) are changed.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketDetailsRequest {

    @Size(max = 150, message = "Title must be at most 150 characters")
    @Pattern(regexp = ".*\\S.*", message = "Title cannot be blank")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private TicketPriority priority;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;
}
