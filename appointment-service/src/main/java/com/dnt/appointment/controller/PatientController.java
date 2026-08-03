package com.dnt.appointment.controller;

import com.dnt.appointment.dto.PatientRequest;
import com.dnt.appointment.dto.PatientResponse;
import com.dnt.appointment.exception.ResourceNotFoundException;
import com.dnt.appointment.integration.AuthServiceSyncClient;
import com.dnt.appointment.integration.ClinicalServiceSyncClient;
import com.dnt.appointment.model.Patient;
import com.dnt.appointment.repository.PatientRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final AuthServiceSyncClient authServiceSyncClient;
    private final ClinicalServiceSyncClient clinicalServiceSyncClient;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<PatientResponse>> getAllPatients(
            @RequestParam(required = false) String q) {
        List<Patient> patients;
        if (q != null && !q.isBlank()) {
            patients = patientRepository.findByFullNameContainingIgnoreCase(q);
        } else {
            patients = patientRepository.findAll();
        }
        return ResponseEntity.ok(patients.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable String publicId) {
        Patient patient = patientRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "publicId", publicId));
        return ResponseEntity.ok(toResponse(patient));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request,
                                                          HttpServletRequest httpRequest) {
        if (patientRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        String userPublicId = request.getUserPublicId();
        if (!StringUtils.hasText(userPublicId)) {
            // No vino ya enlazado a un usuario -> lo creamos/enlazamos automaticamente
            // para cumplir la regla de negocio: todo paciente debe tener cuenta de acceso.
            userPublicId = authServiceSyncClient.ensureUserAccount(
                    extractBearerToken(httpRequest), request.getFullName(), request.getEmail(), "PATIENT");
        }
        Patient patient = Patient.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .userPublicId(userPublicId)
                .build();
        patient = patientRepository.save(patient);
        clinicalServiceSyncClient.syncPatient(
                extractBearerToken(httpRequest), patient.getPublicId(), patient.getFullName(),
                patient.getEmail(), patient.getPhone());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(patient));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable String publicId,
                                                          @Valid @RequestBody PatientRequest request,
                                                          HttpServletRequest httpRequest) {
        Patient patient = patientRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "publicId", publicId));
        patient.setFullName(request.getFullName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient = patientRepository.save(patient);
        clinicalServiceSyncClient.syncPatient(
                extractBearerToken(httpRequest), patient.getPublicId(), patient.getFullName(),
                patient.getEmail(), patient.getPhone());
        return ResponseEntity.ok(toResponse(patient));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable String publicId) {
        Patient patient = patientRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "publicId", publicId));
        patientRepository.delete(patient);
        return ResponseEntity.noContent().build();
    }

    private PatientResponse toResponse(Patient p) {
        return PatientResponse.builder()
                .publicId(p.getPublicId())
                .fullName(p.getFullName())
                .email(p.getEmail())
                .phone(p.getPhone())
                .userPublicId(p.getUserPublicId())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
