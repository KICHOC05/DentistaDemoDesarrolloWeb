package com.dnt.auth.seeder;

import com.dnt.auth.model.Role;
import com.dnt.auth.model.User;
import com.dnt.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Ya existen usuarios en la BD. Omitiendo seeder.");
            return;
        }

        User admin = User.builder()
                .username("admin")
                .email("admin@clinica.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Administrador del Sistema")
                .roles(Set.of(Role.ADMIN))
                .enabled(true)
                .build();

        User recepcionist = User.builder()
                .username("recepcion")
                .email("recepcion@clinica.com")
                .password(passwordEncoder.encode("recepcion123"))
                .fullName("Maria Recepcionista")
                .roles(Set.of(Role.RECEPTIONIST))
                .enabled(true)
                .build();

        User doctor = User.builder()
                .username("doctor1")
                .email("doctor1@clinica.com")
                .password(passwordEncoder.encode("doctor123"))
                .fullName("Dr. Carlos Martinez")
                .roles(Set.of(Role.DOCTOR))
                .enabled(true)
                .build();

        userRepository.save(admin);
        userRepository.save(recepcionist);
        userRepository.save(doctor);

        log.info("Usuarios de prueba creados:");
        log.info("  admin / admin123 (ADMIN)");
        log.info("  recepcion / recepcion123 (RECEPTIONIST)");
        log.info("  doctor1 / doctor123 (DOCTOR)");
    }
}
