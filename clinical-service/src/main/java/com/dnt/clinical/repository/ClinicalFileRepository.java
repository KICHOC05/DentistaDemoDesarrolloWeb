package com.dnt.clinical.repository;

import com.dnt.clinical.model.ClinicalFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicalFileRepository extends JpaRepository<ClinicalFile, Long> {
    List<ClinicalFile> findByPatientPublicIdOrderByUploadedAtDesc(String patientPublicId);
}
