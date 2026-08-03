package com.dnt.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OdontogramRequest {
    @NotBlank(message = "El paciente es obligatorio")
    private String patientPublicId;

    @NotNull(message = "El numero de diente es obligatorio")
    private Integer toothNumber;

    @NotBlank(message = "La condicion es obligatoria")
    private String condition;

    private String notes;
}
