package com.dnt.dentistademo.service;

import com.dnt.dentistademo.dto.AppointmentCreateRequest;
import com.dnt.dentistademo.dto.AppointmentResponse;

import java.util.List;

public interface AppointmentService {

    AppointmentResponse createAppointment(AppointmentCreateRequest request);

    List<AppointmentResponse> getAllAppointments();

    AppointmentResponse getAppointmentByPublicId(String publicId);

    List<AppointmentResponse> getAppointmentsByDoctorPublicId(String doctorPublicId);

    List<AppointmentResponse> getAppointmentsByPatientPublicId(String patientPublicId);

    AppointmentResponse confirmAppointment(String publicId);

    AppointmentResponse cancelAppointment(String publicId);

    AppointmentResponse completeAppointment(String publicId);
}
