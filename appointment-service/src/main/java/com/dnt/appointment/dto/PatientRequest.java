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
public class PatientRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String fullName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email invalido")
    private String email;

    private String phone;

    /**
     * Public id del usuario (auth-service) ya vinculado a este paciente.
     * Si viene informado, no se intenta crear/enlazar un usuario automaticamente
     * (evita bucles cuando la peticion viene de la sincronizacion de auth-service).
     */
    private String userPublicId;
}
