package com.dnt.dentistademo.service;

import com.dnt.dentistademo.dto.AppointmentCreateRequest;
import com.dnt.dentistademo.dto.AppointmentResponse;
import com.dnt.dentistademo.exception.BusinessRuleException;
import com.dnt.dentistademo.exception.ResourceNotFoundException;
import com.dnt.dentistademo.model.Appointment;
import com.dnt.dentistademo.model.AppointmentStatus;
import com.dnt.dentistademo.model.Doctor;
import com.dnt.dentistademo.model.Patient;
import com.dnt.dentistademo.repository.AppointmentRepository;
import com.dnt.dentistademo.repository.DoctorRepository;
import com.dnt.dentistademo.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {
        Patient patient = patientRepository.findByPublicId(request.getPatientPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "publicId", request.getPatientPublicId()));

        Doctor doctor = doctorRepository.findByPublicId(request.getDoctorPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "publicId", request.getDoctorPublicId()));

        boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(
                doctor.getId(), request.getAppointmentDate(), request.getStartTime());
        if (exists) {
            throw new BusinessRuleException(
                    "El doctor ya tiene una cita agendada en esa fecha y hora");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .reason(request.getReason())
                .status(AppointmentStatus.PENDING)
                .build();

        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentByPublicId(String publicId) {
        Appointment appointment = appointmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "publicId", publicId));
        return toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDoctorPublicId(String doctorPublicId) {
        doctorRepository.findByPublicId(doctorPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "publicId", doctorPublicId));
        return appointmentRepository.findByDoctorPublicId(doctorPublicId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatientPublicId(String patientPublicId) {
        patientRepository.findByPublicId(patientPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "publicId", patientPublicId));
        return appointmentRepository.findByPatientPublicId(patientPublicId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentResponse confirmAppointment(String publicId) {
        Appointment appointment = appointmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "publicId", publicId));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessRuleException(
                    "Solo se pueden confirmar citas en estado PENDING");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(String publicId) {
        Appointment appointment = appointmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "publicId", publicId));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "No se puede cancelar una cita que ya ha sido completada");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(String publicId) {
        Appointment appointment = appointmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "publicId", publicId));

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .publicId(appointment.getPublicId())
                .patientName(appointment.getPatient().getFullName())
                .doctorName(appointment.getDoctor().getFullName())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .reason(appointment.getReason())
                .status(appointment.getStatus().name())
                .build();
    }
}
