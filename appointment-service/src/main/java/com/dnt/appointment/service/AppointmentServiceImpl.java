package com.dnt.appointment.service;

import com.dnt.appointment.dto.AppointmentCreateRequest;
import com.dnt.appointment.dto.AppointmentResponse;
import com.dnt.appointment.exception.BusinessRuleException;
import com.dnt.appointment.exception.ResourceNotFoundException;
import com.dnt.appointment.model.Appointment;
import com.dnt.appointment.model.AppointmentStatus;
import com.dnt.appointment.model.Doctor;
import com.dnt.appointment.model.Patient;
import com.dnt.appointment.repository.AppointmentRepository;
import com.dnt.appointment.repository.DoctorRepository;
import com.dnt.appointment.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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

        if (request.getEndTime() != null && !request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessRuleException("La hora de fin debe ser posterior a la hora de inicio");
        }

        boolean overlaps = appointmentRepository.existsOverlappingAppointment(
                doctor.getId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime());
        if (overlaps) {
            throw new BusinessRuleException(
                    "El doctor ya tiene una cita en ese horario");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .status(AppointmentStatus.PENDING)
                .build();

        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointmentsPaged(int page, int size) {
        return appointmentRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appointmentDate", "startTime")))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll(Sort.by(Sort.Direction.DESC, "appointmentDate", "startTime")).stream()
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
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "La cita ya esta cancelada");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointment(String publicId, LocalDate newDate, LocalTime newTime, LocalTime newEndTime) {
        Appointment appointment = appointmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "publicId", publicId));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("No se puede reagendar una cita completada");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleException("No se puede reagendar una cita cancelada");
        }
        if (!newEndTime.isAfter(newTime)) {
            throw new BusinessRuleException("La hora de fin debe ser posterior a la hora de inicio");
        }

        boolean overlaps = appointmentRepository.existsOverlappingAppointmentExcludingId(
                appointment.getDoctor().getId(), newDate, newTime, newEndTime, appointment.getId());
        if (overlaps) {
            throw new BusinessRuleException("El doctor ya tiene una cita en ese horario");
        }

        appointment.setAppointmentDate(newDate);
        appointment.setStartTime(newTime);
        appointment.setEndTime(newEndTime);
        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(String publicId) {
        Appointment appointment = appointmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "publicId", publicId));

        if (appointment.getStatus() != AppointmentStatus.PENDING &&
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessRuleException(
                    "Solo se pueden completar citas en estado PENDING o CONFIRMED");
        }

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
                .endTime(appointment.getEndTime())
                .reason(appointment.getReason())
                .status(appointment.getStatus().name())
                .build();
    }
}
