package com.dnt.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload usado por appointment-service para mantener sincronizada la copia
 * local de pacientes que usa clinical-service (expediente clinico / odontograma).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientSyncRequest {

    @NotBlank(message = "publicId es obligatorio")
    private String publicId;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    @NotBlank(message = "El email es obligatorio")
    private String email;

    private String phone;
}
