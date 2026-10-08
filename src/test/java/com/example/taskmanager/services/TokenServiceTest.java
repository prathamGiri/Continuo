package com.example.taskmanager.services;

import com.example.taskmanager.config.JwtConfig;
import com.example.taskmanager.config.JwtProperties;
import com.example.taskmanager.dto.responses.AuthResponse;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenServiceTest {

    private final String secret = Base64.getEncoder().encodeToString(new byte[32]); // test-only key
    private final JwtProperties props = new JwtProperties(secret, 60);
    private final JwtConfig config = new JwtConfig();
    private final SecretKey key = config.jwtSecretKey(props);
    private final TokenService tokenService = new TokenService(config.jwtEncoder(key), props);
    private final JwtDecoder decoder = config.jwtDecoder(key);

    @Test
    void tokenContainsUserIdAndRole() {
        User user = User.builder().userId(10L).role(UserRole.TeamAdmin).build();

        AuthResponse response = tokenService.generateToken(user);
        Jwt jwt = decoder.decode(response.getAccessToken());

        assertEquals("10", jwt.getSubject());
        assertEquals("TeamAdmin", jwt.getClaimAsString("role"));
        assertEquals(3600, response.getExpiresIn());
    }
}