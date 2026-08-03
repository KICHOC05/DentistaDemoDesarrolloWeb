package com.dnt.appointment.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Regla de negocio: todo Doctor o Paciente debe tener una cuenta de acceso
 * (registro en la tabla "users" de auth-service). Este cliente se encarga de:
 *  - Buscar si ya existe un usuario con ese email (para no duplicar cuentas).
 *  - Si no existe, crear uno automaticamente con el rol correspondiente.
 *
 * Se usa el token del administrador/recepcionista que esta creando el doctor o
 * paciente para autenticar la llamada hacia auth-service (mismo usuario, misma sesion).
 *
 * La llamada de creacion se hace con linkOnly=true para indicarle a auth-service
 * que NO debe, a su vez, volver a llamar a appointment-service (evita bucles).
 */
@Component
@Slf4j
public class AuthServiceSyncClient {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public AuthServiceSyncClient(RestTemplate restTemplate,
                                  @Value("${app.auth-service.url}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    /**
     * Garantiza que exista una cuenta de usuario para este doctor/paciente.
     * Devuelve el publicId del usuario enlazado, o null si no se pudo sincronizar
     * (en cuyo caso el doctor/paciente igual se guarda, solo que sin cuenta enlazada).
     */
    public String ensureUserAccount(String bearerToken, String fullName, String email, String role) {
        String existing = findExistingUserPublicId(bearerToken, email);
        if (existing != null) {
            return existing;
        }
        return createUserAccount(bearerToken, fullName, email, role);
    }

    private String findExistingUserPublicId(String bearerToken, String email) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null) {
                headers.setBearerAuth(bearerToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(
                    authServiceUrl + "/api/auth/users", HttpMethod.GET, entity, List.class);
            List<?> users = response.getBody();
            if (users != null) {
                for (Object o : users) {
                    if (o instanceof Map<?, ?> user) {
                        Object userEmail = user.get("email");
                        if (email.equalsIgnoreCase(String.valueOf(userEmail))) {
                            return String.valueOf(user.get("publicId"));
                        }
                    }
                }
            }
        } catch (RestClientException e) {
            log.warn("No se pudo consultar usuarios existentes en auth-service para {}: {}", email, e.getMessage());
        }
        return null;
    }

    private String createUserAccount(String bearerToken, String fullName, String email, String role) {
        try {
            String username = generateUsername(email);
            Map<String, Object> body = new HashMap<>();
            body.put("username", username);
            body.put("email", email);
            body.put("password", generateTemporaryPassword());
            body.put("fullName", fullName);
            body.put("role", role);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (bearerToken != null) {
                headers.setBearerAuth(bearerToken);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    authServiceUrl + "/api/auth/register?linkOnly=true", entity, Map.class);
            Map<?, ?> created = response.getBody();
            return created != null ? String.valueOf(created.get("publicId")) : null;
        } catch (RestClientException e) {
            log.warn("No se pudo crear automaticamente la cuenta de usuario para {}: {}", email, e.getMessage());
            return null;
        }
    }

    private String generateUsername(String email) {
        String base = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        base = base.toLowerCase().replaceAll("[^a-z0-9._-]", "");
        return base + "." + Integer.toHexString((int) (Math.random() * 0xFFFF));
    }

    private String generateTemporaryPassword() {
        return "Dental" + (int) (Math.random() * 900000 + 100000) + "!";
    }
}
