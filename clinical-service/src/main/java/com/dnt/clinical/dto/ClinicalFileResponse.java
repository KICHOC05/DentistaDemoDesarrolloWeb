package com.dnt.clinical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClinicalFileResponse {
    private Long id;
    private String patientPublicId;
    private String fileName;
    private String contentType;
    private String description;
    private LocalDateTime uploadedAt;
}
