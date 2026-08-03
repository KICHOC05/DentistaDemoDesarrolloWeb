package com.dnt.clinical.repository;

import com.dnt.clinical.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPublicId(String publicId);
    List<Patient> findByFullNameContainingIgnoreCase(String fullName);
}
