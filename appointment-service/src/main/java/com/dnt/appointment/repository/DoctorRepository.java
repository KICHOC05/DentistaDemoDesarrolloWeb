package com.dnt.appointment.repository;

import com.dnt.appointment.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByPublicId(String publicId);
    List<Doctor> findByFullNameContainingIgnoreCase(String fullName);
    List<Doctor> findBySpecialtyContainingIgnoreCase(String specialty);
    boolean existsByEmail(String email);
}
