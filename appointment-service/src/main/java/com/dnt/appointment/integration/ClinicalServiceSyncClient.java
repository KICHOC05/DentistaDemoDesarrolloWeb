package com.dnt.appointment.integration;

import com.dnt.appointment.security.SystemTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Regla de negocio: todo paciente creado en appointment-service debe existir
 * tambien (con el mismo publicId) en clinical-service, para que aparezca en
 * el expediente clinico / odontograma.
 *
 * En entornos de desarrollo cada servicio usa su propia base H2, por lo que
 * esta sincronizacion es indispensable (en produccion con MySQL compartido
 * tambien es inofensiva: simplemente actualiza el mismo registro).
 */
@Component
@Slf4j
public class ClinicalServiceSyncClient {

    private final RestTemplate restTemplate;
    private final SystemTokenProvider systemTokenProvider;
    private final String clinicalServiceUrl;

    public ClinicalServiceSyncClient(RestTemplate restTemplate,
                                      SystemTokenProvider systemTokenProvider,
                                      @Value("${app.clinical-service.url}") String clinicalServiceUrl) {
        this.restTemplate = restTemplate;
        this.systemTokenProvider = systemTokenProvider;
        this.clinicalServiceUrl = clinicalServiceUrl;
    }

    /**
     * @param bearerToken token del usuario que esta creando el paciente; si es null
     *                     (por ejemplo, llamado desde el DataSeeder al arrancar) se
     *                     genera un token interno de sistema.
     */
    public void syncPatient(String bearerToken, String publicId, String fullName, String email, String phone) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("publicId", publicId);
            body.put("fullName", fullName);
            body.put("email", email);
            body.put("phone", phone);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bearerToken != null ? bearerToken : systemTokenProvider.generateSystemToken());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(clinicalServiceUrl + "/api/clinical/patients/sync", entity, Void.class);
        } catch (RestClientException e) {
            log.warn("No se pudo sincronizar el paciente {} con clinical-service: {}", email, e.getMessage());
        }
    }
}
