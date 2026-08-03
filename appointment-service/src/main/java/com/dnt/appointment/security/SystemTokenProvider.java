package com.dnt.appointment.security;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Genera tokens JWT internos, firmados con el mismo secreto compartido por
 * todos los microservicios, para llamadas de servicio-a-servicio que no
 * tienen un usuario autenticado detras (por ejemplo, el DataSeeder al arrancar).
 * No sustituye la autenticacion de usuarios: solo se usa cuando no hay un
 * token real que reenviar.
 */
@Component
public class SystemTokenProvider {

    private SecretKey secretKey;

    @Value("${app.jwt.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        this.secretKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(
                        Base64.getEncoder().encodeToString(secret.getBytes())));
    }

    public String generateSystemToken() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 120_000L); // 2 minutos, solo para la llamada interna

        return Jwts.builder()
                .subject("system-sync")
                .claim("username", "system")
                .claim("fullName", "Sincronizacion interna")
                .claim("roles", List.of("ADMIN"))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }
}
