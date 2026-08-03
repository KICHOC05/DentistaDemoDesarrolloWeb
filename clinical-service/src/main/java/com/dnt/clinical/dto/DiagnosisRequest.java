package com.dnt.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiagnosisRequest {
    @NotBlank(message = "El paciente es obligatorio")
    private String patientPublicId;

    private String diagnosisCode;

    @NotBlank(message = "La descripcion es obligatoria")
    private String description;

    @NotBlank(message = "El diagnostico debe tener un responsable")
    private String diagnosedBy;

    @NotNull
    private LocalDate diagnosedDate;

    private String notes;
}
