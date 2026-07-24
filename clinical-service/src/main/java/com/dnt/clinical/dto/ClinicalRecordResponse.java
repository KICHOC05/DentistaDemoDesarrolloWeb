package com.dnt.clinical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClinicalRecordResponse {
    private String patientPublicId;
    private String bloodType;
    private String allergies;
    private String chronicConditions;
    private String medicalNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
