package com.example.taskmanager.exceptions;

public class InvalidTicketAssignmentException extends RuntimeException {
    public InvalidTicketAssignmentException(String message) {
        super(message);
    }
}
