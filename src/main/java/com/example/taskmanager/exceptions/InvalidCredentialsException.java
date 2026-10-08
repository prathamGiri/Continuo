package com.example.taskmanager.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid Credentials");
    }
}
