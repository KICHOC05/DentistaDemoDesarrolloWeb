package com.dnt.auth.controller;

import com.dnt.auth.dto.AuthResponse;
import com.dnt.auth.dto.LoginRequest;
import com.dnt.auth.dto.RegisterRequest;
import com.dnt.auth.dto.UserResponse;
import com.dnt.auth.integration.AppointmentServiceSyncClient;
import com.dnt.auth.model.Role;
import com.dnt.auth.model.User;
import com.dnt.auth.repository.UserRepository;
import com.dnt.auth.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentServiceSyncClient appointmentServiceSyncClient;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

            Set<String> roles = user.getRoles().stream()
                    .map(Role::name)
                    .collect(Collectors.toSet());

            String token = jwtTokenProvider.generateToken(user.getPublicId(), user.getUsername(), user.getFullName(), roles);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .publicId(user.getPublicId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(roles)
                    .build());
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Usuario o contrasena incorrectos");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  @RequestParam(defaultValue = "false") boolean linkOnly,
                                                  HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(Set.of(request.getRole()))
                .enabled(true)
                .build();

        user = userRepository.save(user);

        // Regla de negocio: DOCTOR/PATIENT deben tener su registro correspondiente
        // en appointment-service. Si linkOnly=true, esta peticion ya viene de
        // appointment-service creando el usuario para un doctor/paciente que el
        // mismo esta creando, asi que no volvemos a llamarlo (evita bucles).
        if (!linkOnly) {
            String token = extractBearerToken(httpRequest);
            if (request.getRole() == Role.DOCTOR) {
                appointmentServiceSyncClient.ensureDoctorRecord(
                        token, user.getPublicId(), user.getFullName(), user.getEmail(), request.getSpecialty());
            } else if (request.getRole() == Role.PATIENT) {
                appointmentServiceSyncClient.ensurePatientRecord(
                        token, user.getPublicId(), user.getFullName(), user.getEmail(), request.getPhone());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toUserResponse(user));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(Authentication authentication) {
        String publicId = authentication.getName();
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Set<String> roles = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(AuthResponse.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build());
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream().map(Role::name).collect(Collectors.toSet()))
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
