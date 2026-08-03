package com.dnt.clinical.repository;

import com.dnt.clinical.model.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    List<Diagnosis> findByPatientPublicIdOrderByDiagnosedDateDesc(String patientPublicId);
}
