package com.dnt.appointment.web.controller;

import com.dnt.appointment.dto.PatientRequest;
import com.dnt.appointment.integration.AuthServiceSyncClient;
import com.dnt.appointment.integration.ClinicalServiceSyncClient;
import com.dnt.appointment.model.Patient;
import com.dnt.appointment.repository.PatientRepository;
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
@RequestMapping("/web/patients")
@RequiredArgsConstructor
public class PatientWebController {

    private final PatientRepository patientRepository;
    private final AuthServiceSyncClient authServiceSyncClient;
    private final ClinicalServiceSyncClient clinicalServiceSyncClient;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String listPatients(@RequestParam(required = false) String q, Model model, HttpServletRequest request) {
        List<Patient> patients;
        if (q != null && !q.isBlank()) {
            patients = patientRepository.findByFullNameContainingIgnoreCase(q);
        } else {
            patients = patientRepository.findAll();
        }
        model.addAttribute("patients", patients);
        model.addAttribute("q", q);
        addToken(model, request);
        return "patients";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String showCreateForm(Model model, HttpServletRequest request) {
        model.addAttribute("patientRequest", new PatientRequest());
        addToken(model, request);
        return "patient-form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String createPatient(@Valid @ModelAttribute("patientRequest") PatientRequest patientRequest,
                                 BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            addToken(model, request);
            return "patient-form";
        }
        String userPublicId = authServiceSyncClient.ensureUserAccount(
                extractToken(request), patientRequest.getFullName(), patientRequest.getEmail(), "PATIENT");
        Patient patient = Patient.builder()
                .fullName(patientRequest.getFullName())
                .email(patientRequest.getEmail())
                .phone(patientRequest.getPhone())
                .userPublicId(userPublicId)
                .build();
        patient = patientRepository.save(patient);
        clinicalServiceSyncClient.syncPatient(
                extractToken(request), patient.getPublicId(), patient.getFullName(),
                patient.getEmail(), patient.getPhone());
        return "redirect:/web/patients";
    }

    @GetMapping("/{publicId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String showEditForm(@PathVariable String publicId, Model model, HttpServletRequest request) {
        Patient patient = patientRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        PatientRequest req = new PatientRequest();
        req.setFullName(patient.getFullName());
        req.setEmail(patient.getEmail());
        req.setPhone(patient.getPhone());
        model.addAttribute("patientRequest", req);
        model.addAttribute("publicId", publicId);
        addToken(model, request);
        return "patient-form";
    }

    @PostMapping("/{publicId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String updatePatient(@PathVariable String publicId,
                                 @Valid @ModelAttribute("patientRequest") PatientRequest patientRequest,
                                 BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("publicId", publicId);
            addToken(model, request);
            return "patient-form";
        }
        Patient patient = patientRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        patient.setFullName(patientRequest.getFullName());
        patient.setEmail(patientRequest.getEmail());
        patient.setPhone(patientRequest.getPhone());
        patient = patientRepository.save(patient);
        clinicalServiceSyncClient.syncPatient(
                extractToken(request), patient.getPublicId(), patient.getFullName(),
                patient.getEmail(), patient.getPhone());
        return "redirect:/web/patients";
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
