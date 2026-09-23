package com.example.taskmanager.exceptions;

public class TeamNotFoundException extends RuntimeException{
    public TeamNotFoundException(Long id) {
        super("Team not found with id: " + id);
    }
}
