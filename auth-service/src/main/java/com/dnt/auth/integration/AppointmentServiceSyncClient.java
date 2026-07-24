package com.dnt.auth.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Regla de negocio: todo usuario con rol DOCTOR o PATIENT debe tener su
 * correspondiente registro (Doctor / Patient) en appointment-service para
 * poder agendar citas y aparecer en el expediente clinico.
 *
 * El userPublicId se envia siempre en la peticion para que appointment-service
 * sepa que el registro ya viene enlazado y NO intente, a su vez, crear otra
 * cuenta de usuario (evita bucles entre los dos servicios).
 */
@Component
@Slf4j
public class AppointmentServiceSyncClient {

    private final RestTemplate restTemplate;
    private final String appointmentServiceUrl;

    public AppointmentServiceSyncClient(RestTemplate restTemplate,
                                         @Value("${app.appointment-service.url}") String appointmentServiceUrl) {
        this.restTemplate = restTemplate;
        this.appointmentServiceUrl = appointmentServiceUrl;
    }

    public void ensureDoctorRecord(String bearerToken, String userPublicId, String fullName,
                                    String email, String specialty) {
        Map<String, Object> body = new HashMap<>();
        body.put("fullName", fullName);
        body.put("email", email);
        body.put("specialty", StringUtils.hasText(specialty) ? specialty : "General");
        body.put("userPublicId", userPublicId);
        post("/api/doctors", bearerToken, body, "doctor");
    }

    public void ensurePatientRecord(String bearerToken, String userPublicId, String fullName,
                                     String email, String phone) {
        Map<String, Object> body = new HashMap<>();
        body.put("fullName", fullName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("userPublicId", userPublicId);
        post("/api/patients", bearerToken, body, "paciente");
    }

    private void post(String path, String bearerToken, Map<String, Object> body, String label) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (bearerToken != null) {
                headers.setBearerAuth(bearerToken);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(appointmentServiceUrl + path, entity, Map.class);
        } catch (RestClientException e) {
            // No bloqueamos la creacion del usuario si appointment-service no responde;
            // solo dejamos constancia para poder enlazar el registro manualmente despues.
            log.warn("No se pudo sincronizar el registro de {} para {}: {}", label, body.get("email"), e.getMessage());
        }
    }
}
