package com.dnt.clinical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TreatmentResponse {
    private Long id;
    private String patientPublicId;
    private Integer toothNumber;
    private String treatmentType;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double cost;
    private String notes;
    private LocalDateTime createdAt;
}
