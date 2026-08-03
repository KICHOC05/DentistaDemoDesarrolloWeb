package com.dnt.clinical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OdontogramResponse {
    private Long id;
    private String patientPublicId;
    private Integer toothNumber;
    private String condition;
    private String notes;
    private LocalDateTime updatedAt;
}
