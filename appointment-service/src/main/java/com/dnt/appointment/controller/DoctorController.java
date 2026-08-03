package com.dnt.appointment.controller;

import com.dnt.appointment.dto.DoctorRequest;
import com.dnt.appointment.dto.DoctorResponse;
import com.dnt.appointment.exception.ResourceNotFoundException;
import com.dnt.appointment.integration.AuthServiceSyncClient;
import com.dnt.appointment.model.Doctor;
import com.dnt.appointment.repository.DoctorRepository;
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
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final AuthServiceSyncClient authServiceSyncClient;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors(
            @RequestParam(required = false) String q) {
        List<Doctor> doctors;
        if (q != null && !q.isBlank()) {
            doctors = doctorRepository.findByFullNameContainingIgnoreCase(q);
        } else {
            doctors = doctorRepository.findAll();
        }
        return ResponseEntity.ok(doctors.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<DoctorResponse> getDoctor(@PathVariable String publicId) {
        Doctor doctor = doctorRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "publicId", publicId));
        return ResponseEntity.ok(toResponse(doctor));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest request,
                                                        HttpServletRequest httpRequest) {
        if (doctorRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        String userPublicId = request.getUserPublicId();
        if (!StringUtils.hasText(userPublicId)) {
            // No vino ya enlazado a un usuario -> lo creamos/enlazamos automaticamente
            // para cumplir la regla de negocio: todo doctor debe tener cuenta de acceso.
            userPublicId = authServiceSyncClient.ensureUserAccount(
                    extractBearerToken(httpRequest), request.getFullName(), request.getEmail(), "DOCTOR");
        }
        Doctor doctor = Doctor.builder()
                .fullName(request.getFullName())
                .specialty(request.getSpecialty())
                .email(request.getEmail())
                .active(true)
                .userPublicId(userPublicId)
                .build();
        doctor = doctorRepository.save(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(doctor));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DoctorResponse> updateDoctor(@PathVariable String publicId,
                                                       @Valid @RequestBody DoctorRequest request) {
        Doctor doctor = doctorRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "publicId", publicId));
        doctor.setFullName(request.getFullName());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setEmail(request.getEmail());
        doctor = doctorRepository.save(doctor);
        return ResponseEntity.ok(toResponse(doctor));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable String publicId) {
        Doctor doctor = doctorRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "publicId", publicId));
        doctor.setActive(false);
        doctorRepository.save(doctor);
        return ResponseEntity.noContent().build();
    }

    private DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
                .publicId(d.getPublicId())
                .fullName(d.getFullName())
                .specialty(d.getSpecialty())
                .email(d.getEmail())
                .active(d.getActive())
                .userPublicId(d.getUserPublicId())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
