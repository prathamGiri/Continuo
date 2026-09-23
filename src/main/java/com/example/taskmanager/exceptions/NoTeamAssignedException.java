package com.example.taskmanager.exceptions;

public class NoTeamAssignedException extends RuntimeException{
    public NoTeamAssignedException() {
        super("User is Not Part of any Team");
    }
}
