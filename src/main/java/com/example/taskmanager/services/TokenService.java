package com.example.taskmanager.services;

import com.example.taskmanager.config.JwtProperties;
import com.example.taskmanager.dto.responses.AuthResponse;
import com.example.taskmanager.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public AuthResponse generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.expiryMinutes(), ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("taskmanager")
                .subject(String.valueOf(user.getUserId()))   // sub = userId
                .claim("role", user.getRole().name())        // e.g. "TeamAdmin"
                .issuedAt(now)                               // iat
                .expiresAt(expiresAt)                        // exp
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return new AuthResponse(token, "Bearer", jwtProperties.expiryMinutes() * 60);
    }
}