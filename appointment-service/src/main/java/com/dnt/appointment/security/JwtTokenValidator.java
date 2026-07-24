package com.dnt.appointment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;

@Component
public class JwtTokenValidator {

    private SecretKey secretKey;

    @Value("${app.jwt.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(
                        Base64.getEncoder().encodeToString(secret.getBytes())));
    }

    public String getPublicIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return parseClaims(token).get("roles", List.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
