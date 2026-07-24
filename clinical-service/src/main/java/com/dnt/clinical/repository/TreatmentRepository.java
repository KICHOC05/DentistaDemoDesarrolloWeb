package com.dnt.clinical.repository;

import com.dnt.clinical.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findByPatientPublicIdOrderByStartDateDesc(String patientPublicId);
    List<Treatment> findByPatientPublicIdAndToothNumber(String patientPublicId, Integer toothNumber);
}
