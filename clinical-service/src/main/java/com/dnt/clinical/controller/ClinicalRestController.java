package com.dnt.clinical.controller;

import com.dnt.clinical.dto.ClinicalFileResponse;
import com.dnt.clinical.dto.ClinicalRecordRequest;
import com.dnt.clinical.dto.ClinicalRecordResponse;
import com.dnt.clinical.dto.DiagnosisRequest;
import com.dnt.clinical.dto.DiagnosisResponse;
import com.dnt.clinical.dto.OdontogramRequest;
import com.dnt.clinical.dto.OdontogramResponse;
import com.dnt.clinical.dto.PatientSyncRequest;
import com.dnt.clinical.dto.TreatmentRequest;
import com.dnt.clinical.dto.TreatmentResponse;
import com.dnt.clinical.model.Patient;
import com.dnt.clinical.repository.PatientRepository;
import com.dnt.clinical.service.ClinicalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clinical")
@RequiredArgsConstructor
public class ClinicalRestController {

    private final ClinicalService clinicalService;
    private final PatientRepository patientRepository;

    /**
     * Sincronizacion interna: appointment-service llama a este endpoint cada vez
     * que crea (o siembra) un paciente, para que exista una copia local en
     * clinical-service con el MISMO publicId. Es idempotente (upsert).
     */
    @PostMapping("/patients/sync")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Void> syncPatient(@Valid @RequestBody PatientSyncRequest request) {
        Patient patient = patientRepository.findByPublicId(request.getPublicId())
                .orElseGet(() -> Patient.builder().publicId(request.getPublicId()).build());
        patient.setFullName(request.getFullName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patientRepository.save(patient);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/records/{patientPublicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ClinicalRecordResponse> getRecord(@PathVariable String patientPublicId) {
        ClinicalRecordResponse record = clinicalService.getRecordByPatient(patientPublicId);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    @PostMapping("/records")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ClinicalRecordResponse> createOrUpdateRecord(
            @Valid @RequestBody ClinicalRecordRequest request) {
        return ResponseEntity.ok(clinicalService.getOrCreateRecord(request));
    }

    @GetMapping("/records/{patientPublicId}/diagnoses")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<DiagnosisResponse>> getDiagnoses(@PathVariable String patientPublicId) {
        return ResponseEntity.ok(clinicalService.getDiagnoses(patientPublicId));
    }

    @PostMapping("/records/{patientPublicId}/diagnoses")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<DiagnosisResponse> addDiagnosis(
            @PathVariable String patientPublicId,
            @Valid @RequestBody DiagnosisRequest request) {
        request.setPatientPublicId(patientPublicId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicalService.addDiagnosis(request));
    }

    @GetMapping("/records/{patientPublicId}/treatments")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<TreatmentResponse>> getTreatments(@PathVariable String patientPublicId) {
        return ResponseEntity.ok(clinicalService.getTreatments(patientPublicId));
    }

    @PostMapping("/records/{patientPublicId}/treatments")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<TreatmentResponse> addTreatment(
            @PathVariable String patientPublicId,
            @Valid @RequestBody TreatmentRequest request) {
        request.setPatientPublicId(patientPublicId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicalService.addTreatment(request));
    }

    @PatchMapping("/treatments/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<TreatmentResponse> updateTreatmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(clinicalService.updateTreatmentStatus(id, body.get("status")));
    }

    @GetMapping("/odontogram/{patientPublicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<OdontogramResponse>> getOdontogram(@PathVariable String patientPublicId) {
        return ResponseEntity.ok(clinicalService.getOdontogram(patientPublicId));
    }

    @PutMapping("/odontogram")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<OdontogramResponse> updateTooth(
            @Valid @RequestBody OdontogramRequest request) {
        return ResponseEntity.ok(clinicalService.updateTooth(request));
    }

    @GetMapping("/records/{patientPublicId}/files")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<ClinicalFileResponse>> getFiles(@PathVariable String patientPublicId) {
        return ResponseEntity.ok(clinicalService.getFiles(patientPublicId));
    }

    @PostMapping("/records/{patientPublicId}/files")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ClinicalFileResponse> uploadFile(
            @PathVariable String patientPublicId,
            @RequestParam String fileName,
            @RequestParam String contentType,
            @RequestParam String data,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicalService.addFile(patientPublicId, fileName, contentType, data, description));
    }
}
