package com.dnt.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClinicalRecordRequest {
    @NotBlank(message = "El paciente es obligatorio")
    private String patientPublicId;

    private String bloodType;
    private String allergies;
    private String chronicConditions;
    private String medicalNotes;
}
