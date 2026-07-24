package com.dnt.clinical.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnoses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String patientPublicId;

    @Column(length = 20)
    private String diagnosisCode;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String diagnosedBy;

    @Column(nullable = false)
    private LocalDate diagnosedDate;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (diagnosedDate == null) diagnosedDate = LocalDate.now();
    }
}
