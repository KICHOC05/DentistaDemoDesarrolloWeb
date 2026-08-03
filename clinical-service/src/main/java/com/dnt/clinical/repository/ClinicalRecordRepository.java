package com.dnt.clinical.repository;

import com.dnt.clinical.model.ClinicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, Long> {
    Optional<ClinicalRecord> findByPatientPublicId(String patientPublicId);
    boolean existsByPatientPublicId(String patientPublicId);
}
