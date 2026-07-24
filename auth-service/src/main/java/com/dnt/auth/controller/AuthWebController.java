package com.dnt.auth.controller;

import com.dnt.auth.dto.LoginRequest;
import com.dnt.auth.dto.RegisterRequest;
import com.dnt.auth.dto.UserResponse;
import com.dnt.auth.integration.AppointmentServiceSyncClient;
import com.dnt.auth.model.Role;
import com.dnt.auth.model.User;
import com.dnt.auth.repository.UserRepository;
import com.dnt.auth.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentServiceSyncClient appointmentServiceSyncClient;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                        BindingResult result, HttpServletResponse response, Model model) {
        if (result.hasErrors()) {
            return "login";
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

            Set<String> roles = user.getRoles().stream()
                    .map(Role::name)
                    .collect(Collectors.toSet());

            String token = jwtTokenProvider.generateToken(user.getPublicId(), user.getUsername(), user.getFullName(), roles);

            Cookie tokenCookie = new Cookie("token", token);
            tokenCookie.setPath("/");
            tokenCookie.setHttpOnly(true);
            tokenCookie.setMaxAge(86400);
            response.addCookie(tokenCookie);

            return "redirect:/web/dashboard";
        } catch (BadCredentialsException e) {
            model.addAttribute("error", "Usuario o contrasena incorrectos");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        // Spring Security (JwtAuthenticationFilter + .anyRequest().authenticated())
        // ya garantiza que si llegamos aqui, la peticion esta autenticada.
        // Solo extraemos el token para leer datos a mostrar, sin volver a
        // decidir si se permite el acceso (evita una doble validacion que
        // podia divergir de la decision real de Spring Security).
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            model.addAttribute("userFullName", jwtTokenProvider.getFullNameFromToken(token));
            model.addAttribute("userRoles", jwtTokenProvider.getRolesFromToken(token));
        }
        return "dashboard";
    }

    @GetMapping("/users")
    public String listUsers(HttpServletRequest request, Model model) {
        // Proteccion real: SecurityConfig ya exige rol ADMIN/RECEPTIONIST para /web/users/**.
        model.addAttribute("users", userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList());
        model.addAttribute("registerRequest", new RegisterRequest());
        return "users";
    }

    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                             BindingResult result, HttpServletRequest servletRequest, Model model) {
        // Proteccion real: SecurityConfig ya exige rol ADMIN/RECEPTIONIST para /web/users/**.
        String token = extractToken(servletRequest);
        if (result.hasErrors()) {
            model.addAttribute("users", userRepository.findAll().stream()
                    .map(this::toUserResponse)
                    .toList());
            return "users";
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            model.addAttribute("error", "El nombre de usuario ya existe");
            model.addAttribute("users", userRepository.findAll().stream()
                    .map(this::toUserResponse)
                    .toList());
            return "users";
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            model.addAttribute("error", "El email ya esta registrado");
            model.addAttribute("users", userRepository.findAll().stream()
                    .map(this::toUserResponse)
                    .toList());
            return "users";
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

        // Regla de negocio: si el nuevo usuario es DOCTOR o PATIENT, debe quedar
        // reflejado tambien en appointment-service (tabla de doctores/pacientes).
        if (request.getRole() == Role.DOCTOR) {
            appointmentServiceSyncClient.ensureDoctorRecord(
                    token, user.getPublicId(), user.getFullName(), user.getEmail(), request.getSpecialty());
        } else if (request.getRole() == Role.PATIENT) {
            appointmentServiceSyncClient.ensurePatientRecord(
                    token, user.getPublicId(), user.getFullName(), user.getEmail(), request.getPhone());
        }

        return "redirect:/web/users";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie tokenCookie = new Cookie("token", null);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);
        return "redirect:/web/login";
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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
