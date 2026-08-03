package com.dnt.appointment.service;

import com.dnt.appointment.dto.AppointmentCreateRequest;
import com.dnt.appointment.dto.AppointmentResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {

    AppointmentResponse createAppointment(AppointmentCreateRequest request);

    Page<AppointmentResponse> getAllAppointmentsPaged(int page, int size);

    List<AppointmentResponse> getAllAppointments();

    AppointmentResponse getAppointmentByPublicId(String publicId);

    List<AppointmentResponse> getAppointmentsByDoctorPublicId(String doctorPublicId);

    List<AppointmentResponse> getAppointmentsByPatientPublicId(String patientPublicId);

    AppointmentResponse confirmAppointment(String publicId);

    AppointmentResponse cancelAppointment(String publicId);

    AppointmentResponse completeAppointment(String publicId);

    AppointmentResponse rescheduleAppointment(String publicId, LocalDate newDate, LocalTime newTime, LocalTime newEndTime);
}
