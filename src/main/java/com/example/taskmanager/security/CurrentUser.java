package com.example.taskmanager.security;

import org.springframework.security.oauth2.jwt.Jwt;

/** Reads the caller's user id from the validated JWT ("sub" claim). */
public final class CurrentUser {

    private CurrentUser() {}

    public static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
