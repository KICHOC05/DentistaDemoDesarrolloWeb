package com.dnt.dentistademo.seeder;

import com.dnt.dentistademo.model.Doctor;
import com.dnt.dentistademo.model.Patient;
import com.dnt.dentistademo.repository.DoctorRepository;
import com.dnt.dentistademo.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public void run(String... args) {
        if (patientRepository.count() > 0) {
            log.info("La base de datos ya contiene datos. Omitiendo DataSeeder.");
            return;
        }

        Patient patient1 = Patient.builder()
                .fullName("Juan Perez")
                .email("juan.perez@email.com")
                .phone("555-1001")
                .build();

        Patient patient2 = Patient.builder()
                .fullName("Maria Garcia")
                .email("maria.garcia@email.com")
                .phone("555-1002")
                .build();

        patientRepository.save(patient1);
        patientRepository.save(patient2);

        Doctor doctor1 = Doctor.builder()
                .fullName("Dra. Ana Lopez")
                .specialty("Ortodoncia")
                .email("ana.lopez@clinica.com")
                .active(true)
                .build();

        Doctor doctor2 = Doctor.builder()
                .fullName("Dr. Carlos Martinez")
                .specialty("Endodoncia")
                .email("carlos.martinez@clinica.com")
                .active(true)
                .build();

        doctorRepository.save(doctor1);
        doctorRepository.save(doctor2);

        log.info("Datos de prueba creados exitosamente.");
        log.info("Pacientes: {} ({}) - {} ({})",
                patient1.getFullName(), patient1.getPublicId(),
                patient2.getFullName(), patient2.getPublicId());
        log.info("Doctores: {} ({}) - {} ({})",
                doctor1.getFullName(), doctor1.getPublicId(),
                doctor2.getFullName(), doctor2.getPublicId());
    }
}
