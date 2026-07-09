package com.dnt.dentistademo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreateRequest {

    @NotBlank(message = "El publicId del paciente es obligatorio")
    private String patientPublicId;

    @NotBlank(message = "El publicId del doctor es obligatorio")
    private String doctorPublicId;

    @NotNull(message = "La fecha de la cita es obligatoria")
    private LocalDate appointmentDate;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotBlank(message = "El motivo de la cita es obligatorio")
    private String reason;
}
