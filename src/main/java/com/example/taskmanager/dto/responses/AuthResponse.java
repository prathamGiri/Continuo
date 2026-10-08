package com.example.taskmanager.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
