package com.example.taskmanager.exceptions;

import com.example.taskmanager.enums.UserRole;

public class DuplicateRoleAssignmentException extends RuntimeException{
    public DuplicateRoleAssignmentException(UserRole role) {
        super("User Already has the role: " + role);
    }
}
