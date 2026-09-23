package com.example.taskmanager.exceptions;

public class DuplicateTeamAssignmentException extends RuntimeException{
    public DuplicateTeamAssignmentException(String team) {
        super("User Already a Part of the Team!" + team);
    }
}
