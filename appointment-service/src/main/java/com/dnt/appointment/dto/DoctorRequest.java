package com.dnt.appointment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String fullName;

    @NotBlank(message = "La especialidad es obligatoria")
    private String specialty;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email invalido")
    private String email;

    /**
     * Public id del usuario (auth-service) ya vinculado a este doctor.
     * Si viene informado, no se intenta crear/enlazar un usuario automaticamente
     * (evita bucles cuando la peticion viene de la sincronizacion de auth-service).
     */
    private String userPublicId;
}
