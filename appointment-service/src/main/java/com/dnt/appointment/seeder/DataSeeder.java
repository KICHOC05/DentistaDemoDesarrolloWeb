package com.dnt.appointment.seeder;

import com.dnt.appointment.integration.ClinicalServiceSyncClient;
import com.dnt.appointment.model.Doctor;
import com.dnt.appointment.model.Patient;
import com.dnt.appointment.repository.DoctorRepository;
import com.dnt.appointment.repository.PatientRepository;
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
    private final ClinicalServiceSyncClient clinicalServiceSyncClient;

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

        // Regla de negocio: los pacientes deben existir tambien en clinical-service
        // (que en desarrollo tiene su propia base de datos separada). Como aqui no
        // hay un usuario logueado (es el arranque de la aplicacion), la sincronizacion
        // usa un token interno de sistema en lugar del token de un usuario real.
        clinicalServiceSyncClient.syncPatient(
                null, patient1.getPublicId(), patient1.getFullName(), patient1.getEmail(), patient1.getPhone());
        clinicalServiceSyncClient.syncPatient(
                null, patient2.getPublicId(), patient2.getFullName(), patient2.getEmail(), patient2.getPhone());

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

