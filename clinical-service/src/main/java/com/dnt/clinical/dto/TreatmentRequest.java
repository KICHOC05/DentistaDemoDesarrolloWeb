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
public class TreatmentRequest {
    @NotBlank(message = "El paciente es obligatorio")
    private String patientPublicId;

    @NotNull(message = "El numero de diente es obligatorio")
    private Integer toothNumber;

    @NotBlank(message = "El tipo de tratamiento es obligatorio")
    private String treatmentType;

    @NotBlank(message = "La descripcion es obligatoria")
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;
    private Double cost;
    private String notes;
}
