package com.dnt.clinical.repository;

import com.dnt.clinical.model.Odontogram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OdontogramRepository extends JpaRepository<Odontogram, Long> {
    List<Odontogram> findByPatientPublicId(String patientPublicId);
    Optional<Odontogram> findByPatientPublicIdAndToothNumber(String patientPublicId, Integer toothNumber);
}
