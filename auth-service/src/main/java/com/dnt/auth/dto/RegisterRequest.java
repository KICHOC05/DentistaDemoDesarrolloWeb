package com.dnt.auth.dto;

import com.dnt.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email invalido")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    @NotNull(message = "El rol es obligatorio")
    private Role role;

    /** Solo aplica cuando role = DOCTOR. Se usa para crear su registro en appointment-service. */
    private String specialty;

    /** Solo aplica cuando role = PATIENT. Se usa para crear su registro en appointment-service. */
    private String phone;
}
