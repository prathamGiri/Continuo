package com.example.taskmanager.exceptions;

import com.example.taskmanager.enums.UserRole;

public class NoRoleFoundException extends RuntimeException {
    public NoRoleFoundException(UserRole role) {
        super("No Role Found :" + role);
    }
}
