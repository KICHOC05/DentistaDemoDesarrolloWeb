package com.dnt.catalog;

import com.dnt.catalog.dto.*;
import com.dnt.catalog.seeder.DataSeeder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(DataSeeder.class)
class CatalogServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/catalog/services";
    private static final String JWT_SECRET = "ClinicaDentalSaaS2026SecretKeyForJWTTokenGenerationHS256";

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(
                Base64.getEncoder().encodeToString(JWT_SECRET.getBytes(StandardCharsets.UTF_8))
        );
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private String generateToken(String publicId, String username, String fullName, List<String> roles) {
        return Jwts.builder()
                .subject(publicId)
                .claim("username", username)
                .claim("fullName", fullName)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey())
                .compact();
    }

    private String adminToken() {
        return generateToken("admin-uuid", "admin", "Admin User",
                List.of("ADMIN"));
    }

    private String receptionistToken() {
        return generateToken("receptionist-uuid", "recepcion", "Receptionist User",
                List.of("RECEPTIONIST"));
    }

    private String doctorToken() {
        return generateToken("doctor-uuid", "doctor1", "Doctor User",
                List.of("DOCTOR"));
    }

    private String patientToken() {
        return generateToken("patient-uuid", "patient1", "Patient User",
                List.of("PATIENT"));
    }

    // ─── Create ──────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/catalog/services — Crear servicio")
    class CreateService {

        @Test
        @DisplayName("Debe crear un servicio dental correctamente")
        void shouldCreateService() throws Exception {
            CreateDentalServiceRequest request = CreateDentalServiceRequest.builder()
                    .name("Limpieza dental")
                    .description("Limpieza general y eliminacion de sarro")
                    .price(new BigDecimal("650.00"))
                    .durationMinutes(45)
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.publicId").isNotEmpty())
                    .andExpect(jsonPath("$.name").value("Limpieza dental"))
                    .andExpect(jsonPath("$.price").value(650.0))
                    .andExpect(jsonPath("$.durationMinutes").value(45))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("Debe rechazar un nombre vacio")
        void shouldRejectEmptyName() throws Exception {
            CreateDentalServiceRequest request = CreateDentalServiceRequest.builder()
                    .name("")
                    .price(new BigDecimal("100.00"))
                    .durationMinutes(30)
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.name").isNotEmpty());
        }

        @Test
        @DisplayName("Debe rechazar un precio negativo")
        void shouldRejectNegativePrice() throws Exception {
            CreateDentalServiceRequest request = CreateDentalServiceRequest.builder()
                    .name("Servicio con precio invalido")
                    .price(new BigDecimal("-50.00"))
                    .durationMinutes(30)
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.price").isNotEmpty());
        }

        @Test
        @DisplayName("Debe rechazar una duracion menor que uno")
        void shouldRejectZeroDuration() throws Exception {
            CreateDentalServiceRequest request = CreateDentalServiceRequest.builder()
                    .name("Servicio con duracion invalida")
                    .price(new BigDecimal("100.00"))
                    .durationMinutes(0)
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.durationMinutes").isNotEmpty());
        }

        @Test
        @DisplayName("Debe rechazar nombres duplicados")
        void shouldRejectDuplicateName() throws Exception {
            CreateDentalServiceRequest request = CreateDentalServiceRequest.builder()
                    .name("Ortodoncia")
                    .price(new BigDecimal("1500.00"))
                    .durationMinutes(60)
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    // ─── List all ────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/catalog/services — Listar todos")
    class ListAll {

        @Test
        @DisplayName("Debe listar todos los servicios (Admin)")
        void shouldListAll() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Debe devolver 403 si no es admin/receptionist")
        void shouldDenyNonAdmin() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + doctorToken()))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── Active ──────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/catalog/services/active — Listar activos")
    class ListActive {

        @Test
        @DisplayName("Debe listar servicios activos (cualquier autenticado)")
        void shouldListActive() throws Exception {
            // Create one active service
            CreateDentalServiceRequest req = CreateDentalServiceRequest.builder()
                    .name("Servicio activo test")
                    .price(new BigDecimal("100.00"))
                    .durationMinutes(30)
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());

            // Patient can see active
            mockMvc.perform(get(BASE_URL + "/active")
                            .header("Authorization", "Bearer " + patientToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Debe negar acceso sin token a activos")
        void shouldDenyAnonymousActive() throws Exception {
            mockMvc.perform(get(BASE_URL + "/active"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── Get by publicId ─────────────────────────────────

    @Nested
    @DisplayName("GET /api/catalog/services/{publicId} — Obtener por ID")
    class GetById {

        @Test
        @DisplayName("Debe devolver servicio existente")
        void shouldReturnExisting() throws Exception {
            CreateDentalServiceRequest req = CreateDentalServiceRequest.builder()
                    .name("Consulta general")
                    .price(new BigDecimal("350.00"))
                    .durationMinutes(30)
                    .build();

            String responseJson = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String publicId = objectMapper.readTree(responseJson).get("publicId").asText();

            mockMvc.perform(get(BASE_URL + "/" + publicId)
                            .header("Authorization", "Bearer " + doctorToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.publicId").value(publicId))
                    .andExpect(jsonPath("$.name").value("Consulta general"));
        }

        @Test
        @DisplayName("Debe devolver 404 si no existe")
        void shouldReturn404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/non-existent-uuid")
                            .header("Authorization", "Bearer " + adminToken()))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── Update ──────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/catalog/services/{publicId} — Actualizar")
    class UpdateService {

        @Test
        @DisplayName("Debe actualizar un servicio")
        void shouldUpdate() throws Exception {
            CreateDentalServiceRequest req = CreateDentalServiceRequest.builder()
                    .name("Servicio original")
                    .price(new BigDecimal("200.00"))
                    .durationMinutes(20)
                    .build();

            String responseJson = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String publicId = objectMapper.readTree(responseJson).get("publicId").asText();

            UpdateDentalServiceRequest updateReq = UpdateDentalServiceRequest.builder()
                    .name("Servicio actualizado")
                    .price(new BigDecimal("300.00"))
                    .durationMinutes(25)
                    .build();

            mockMvc.perform(put(BASE_URL + "/" + publicId)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Servicio actualizado"))
                    .andExpect(jsonPath("$.price").value(300.0))
                    .andExpect(jsonPath("$.durationMinutes").value(25));
        }
    }

    // ─── Change status ───────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/catalog/services/{publicId}/status — Cambiar estado")
    class ChangeStatus {

        @Test
        @DisplayName("Debe desactivar un servicio")
        void shouldDeactivate() throws Exception {
            CreateDentalServiceRequest req = CreateDentalServiceRequest.builder()
                    .name("Servicio a desactivar")
                    .price(new BigDecimal("100.00"))
                    .durationMinutes(10)
                    .build();

            String responseJson = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String publicId = objectMapper.readTree(responseJson).get("publicId").asText();

            mockMvc.perform(patch(BASE_URL + "/" + publicId + "/status")
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"active\": false}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false));
        }
    }

    // ─── Delete ──────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/catalog/services/{publicId} — Eliminar")
    class DeleteService {

        @Test
        @DisplayName("Debe eliminar un servicio (Admin)")
        void shouldDelete() throws Exception {
            CreateDentalServiceRequest req = CreateDentalServiceRequest.builder()
                    .name("Servicio a eliminar")
                    .price(new BigDecimal("50.00"))
                    .durationMinutes(5)
                    .build();

            String responseJson = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + adminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String publicId = objectMapper.readTree(responseJson).get("publicId").asText();

            mockMvc.perform(delete(BASE_URL + "/" + publicId)
                            .header("Authorization", "Bearer " + adminToken()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get(BASE_URL + "/" + publicId)
                            .header("Authorization", "Bearer " + adminToken()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Debe devolver 403 si no es Admin")
        void shouldDenyNonAdmin() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/some-uuid")
                            .header("Authorization", "Bearer " + receptionistToken()))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── Security ────────────────────────────────────────

    @Nested
    @DisplayName("Seguridad JWT")
    class Security {

        @Test
        @DisplayName("Debe devolver 401 sin token en ruta protegida")
        void shouldReturn401WithoutToken() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Debe devolver 403 para Doctor en rutas administrativas")
        void shouldDenyDoctorOnAdminRoutes() throws Exception {
            CreateDentalServiceRequest req = CreateDentalServiceRequest.builder()
                    .name("Test")
                    .price(new BigDecimal("10.00"))
                    .durationMinutes(10)
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + doctorToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debe permitir acceso publico a health")
        void shouldAllowPublicHealth() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }
}
