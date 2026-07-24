package com.dnt.clinical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiagnosisResponse {
    private Long id;
    private String patientPublicId;
    private String diagnosisCode;
    private String description;
    private String diagnosedBy;
    private LocalDate diagnosedDate;
    private String notes;
    private LocalDateTime createdAt;
}
