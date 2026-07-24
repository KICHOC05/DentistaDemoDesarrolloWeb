package com.dnt.appointment.repository;

import com.dnt.appointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByPublicId(String publicId);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status <> 'CANCELLED' AND a.startTime < :endTime AND :startTime < a.endTime")
    boolean existsOverlappingAppointment(@Param("doctorId") Long doctorId, @Param("date") LocalDate date,
                                          @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status <> 'CANCELLED' AND a.id <> :excludeId AND a.startTime < :endTime AND :startTime < a.endTime")
    boolean existsOverlappingAppointmentExcludingId(@Param("doctorId") Long doctorId, @Param("date") LocalDate date,
                                                     @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
                                                     @Param("excludeId") Long excludeId);

    List<Appointment> findByDoctorPublicId(String doctorPublicId);

    List<Appointment> findByPatientPublicId(String patientPublicId);
}
