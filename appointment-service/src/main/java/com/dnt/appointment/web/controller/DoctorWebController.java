package com.dnt.appointment.web.controller;

import com.dnt.appointment.dto.DoctorRequest;
import com.dnt.appointment.integration.AuthServiceSyncClient;
import com.dnt.appointment.model.Doctor;
import com.dnt.appointment.repository.DoctorRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/web/doctors")
@RequiredArgsConstructor
public class DoctorWebController {

    private final DoctorRepository doctorRepository;
    private final AuthServiceSyncClient authServiceSyncClient;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String listDoctors(@RequestParam(required = false) String q, Model model, HttpServletRequest request) {
        List<Doctor> doctors;
        if (q != null && !q.isBlank()) {
            doctors = doctorRepository.findByFullNameContainingIgnoreCase(q);
        } else {
            doctors = doctorRepository.findAll();
        }
        model.addAttribute("doctors", doctors);
        model.addAttribute("q", q);
        addToken(model, request);
        return "doctors";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model, HttpServletRequest request) {
        model.addAttribute("doctorRequest", new DoctorRequest());
        addToken(model, request);
        return "doctor-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createDoctor(@Valid @ModelAttribute("doctorRequest") DoctorRequest doctorRequest,
                                BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            addToken(model, request);
            return "doctor-form";
        }
        String userPublicId = authServiceSyncClient.ensureUserAccount(
                extractToken(request), doctorRequest.getFullName(), doctorRequest.getEmail(), "DOCTOR");
        Doctor doctor = Doctor.builder()
                .fullName(doctorRequest.getFullName())
                .specialty(doctorRequest.getSpecialty())
                .email(doctorRequest.getEmail())
                .active(true)
                .userPublicId(userPublicId)
                .build();
        doctorRepository.save(doctor);
        return "redirect:/web/doctors";
    }

    @GetMapping("/{publicId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable String publicId, Model model, HttpServletRequest request) {
        Doctor doctor = doctorRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
        DoctorRequest req = new DoctorRequest();
        req.setFullName(doctor.getFullName());
        req.setSpecialty(doctor.getSpecialty());
        req.setEmail(doctor.getEmail());
        model.addAttribute("doctorRequest", req);
        model.addAttribute("publicId", publicId);
        addToken(model, request);
        return "doctor-form";
    }

    @PostMapping("/{publicId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateDoctor(@PathVariable String publicId,
                                @Valid @ModelAttribute("doctorRequest") DoctorRequest doctorRequest,
                                BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("publicId", publicId);
            addToken(model, request);
            return "doctor-form";
        }
        Doctor doctor = doctorRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
        doctor.setFullName(doctorRequest.getFullName());
        doctor.setSpecialty(doctorRequest.getSpecialty());
        doctor.setEmail(doctorRequest.getEmail());
        doctorRepository.save(doctor);
        return "redirect:/web/doctors";
    }

    @GetMapping("/{publicId}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleDoctor(@PathVariable String publicId) {
        Doctor doctor = doctorRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
        doctor.setActive(!doctor.getActive());
        doctorRepository.save(doctor);
        return "redirect:/web/doctors";
    }

    private void addToken(Model model, HttpServletRequest request) {
        String token = extractToken(request);
        model.addAttribute("token", token != null ? token : "");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            model.addAttribute("userRoles", roles);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) return tokenParam;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) return cookie.getValue();
            }
        }
        return null;
    }
}
