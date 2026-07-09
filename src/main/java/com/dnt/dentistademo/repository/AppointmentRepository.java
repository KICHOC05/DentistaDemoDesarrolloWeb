package com.dnt.dentistademo.repository;

import com.dnt.dentistademo.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByPublicId(String publicId);

    boolean existsByDoctorIdAndAppointmentDateAndStartTime(Long doctorId, LocalDate appointmentDate, LocalTime startTime);

    List<Appointment> findByDoctorPublicId(String doctorPublicId);

    List<Appointment> findByPatientPublicId(String patientPublicId);
}
