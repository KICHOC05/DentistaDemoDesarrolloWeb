package com.dnt.appointment.controller;

import com.dnt.appointment.dto.AppointmentCreateRequest;
import com.dnt.appointment.dto.AppointmentResponse;
import com.dnt.appointment.dto.RescheduleRequest;
import com.dnt.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<?> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (page >= 0 && size > 0) {
            Page<AppointmentResponse> paged = appointmentService.getAllAppointmentsPaged(page, size);
            return ResponseEntity.ok(paged);
        }
        List<AppointmentResponse> responses = appointmentService.getAllAppointments();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> getAppointmentByPublicId(
            @PathVariable String publicId) {
        AppointmentResponse response = appointmentService.getAppointmentByPublicId(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorPublicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(
            @PathVariable String doctorPublicId) {
        List<AppointmentResponse> responses = appointmentService.getAppointmentsByDoctorPublicId(doctorPublicId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/patient/{patientPublicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(
            @PathVariable String patientPublicId) {
        List<AppointmentResponse> responses = appointmentService.getAppointmentsByPatientPublicId(patientPublicId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{publicId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            @PathVariable String publicId) {
        AppointmentResponse response = appointmentService.confirmAppointment(publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable String publicId) {
        AppointmentResponse response = appointmentService.cancelAppointment(publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable String publicId) {
        AppointmentResponse response = appointmentService.completeAppointment(publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable String publicId,
            @Valid @RequestBody RescheduleRequest request) {
        AppointmentResponse response = appointmentService.rescheduleAppointment(
                publicId, request.getNewDate(), request.getNewTime(), request.getNewEndTime());
        return ResponseEntity.ok(response);
    }
}
