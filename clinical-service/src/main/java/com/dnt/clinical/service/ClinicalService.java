package com.dnt.clinical.service;

import com.dnt.clinical.dto.ClinicalFileResponse;
import com.dnt.clinical.dto.ClinicalRecordRequest;
import com.dnt.clinical.dto.ClinicalRecordResponse;
import com.dnt.clinical.dto.DiagnosisRequest;
import com.dnt.clinical.dto.DiagnosisResponse;
import com.dnt.clinical.dto.OdontogramRequest;
import com.dnt.clinical.dto.OdontogramResponse;
import com.dnt.clinical.dto.TreatmentRequest;
import com.dnt.clinical.dto.TreatmentResponse;

import java.util.List;

public interface ClinicalService {
    ClinicalRecordResponse getOrCreateRecord(ClinicalRecordRequest request);
    ClinicalRecordResponse getRecordByPatient(String patientPublicId);

    List<DiagnosisResponse> getDiagnoses(String patientPublicId);
    DiagnosisResponse addDiagnosis(DiagnosisRequest request);

    List<TreatmentResponse> getTreatments(String patientPublicId);
    TreatmentResponse addTreatment(TreatmentRequest request);
    TreatmentResponse updateTreatmentStatus(Long id, String status);

    List<OdontogramResponse> getOdontogram(String patientPublicId);
    OdontogramResponse updateTooth(OdontogramRequest request);

    List<ClinicalFileResponse> getFiles(String patientPublicId);
    ClinicalFileResponse addFile(String patientPublicId, String fileName, String contentType, String data, String description);
}
