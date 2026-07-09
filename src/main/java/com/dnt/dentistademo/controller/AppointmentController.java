package com.dnt.dentistademo.controller;

import com.dnt.dentistademo.dto.AppointmentCreateRequest;
import com.dnt.dentistademo.dto.AppointmentResponse;
import com.dnt.dentistademo.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        List<AppointmentResponse> responses = appointmentService.getAllAppointments();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<AppointmentResponse> getAppointmentByPublicId(
            @PathVariable String publicId) {
        AppointmentResponse response = appointmentService.getAppointmentByPublicId(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorPublicId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(
            @PathVariable String doctorPublicId) {
        List<AppointmentResponse> responses = appointmentService.getAppointmentsByDoctorPublicId(doctorPublicId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/patient/{patientPublicId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(
            @PathVariable String patientPublicId) {
        List<AppointmentResponse> responses = appointmentService.getAppointmentsByPatientPublicId(patientPublicId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{publicId}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            @PathVariable String publicId) {
        AppointmentResponse response = appointmentService.confirmAppointment(publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable String publicId) {
        AppointmentResponse response = appointmentService.cancelAppointment(publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable String publicId) {
        AppointmentResponse response = appointmentService.completeAppointment(publicId);
        return ResponseEntity.ok(response);
    }
}
