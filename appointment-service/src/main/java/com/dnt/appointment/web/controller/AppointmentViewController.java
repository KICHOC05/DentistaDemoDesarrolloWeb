package com.dnt.appointment.web.controller;

import com.dnt.appointment.dto.AppointmentCreateRequest;
import com.dnt.appointment.dto.AppointmentResponse;
import com.dnt.appointment.repository.DoctorRepository;
import com.dnt.appointment.repository.PatientRepository;
import com.dnt.appointment.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/web/appointments")
@RequiredArgsConstructor
public class AppointmentViewController {

    private final AppointmentService appointmentService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String listAppointments(@RequestParam(defaultValue = "0") int page,
                                    Model model, HttpServletRequest request) {
        int size = 20;
        Page<AppointmentResponse> appointmentPage = appointmentService.getAllAppointmentsPaged(page, size);
        model.addAttribute("appointments", appointmentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", appointmentPage.getTotalPages());
        model.addAttribute("totalElements", appointmentPage.getTotalElements());
        addUserInfo(model, request);
        return "appointments";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String showCreateForm(Model model, HttpServletRequest request) {
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("request", new AppointmentCreateRequest());
        addUserInfo(model, request);
        return "appointment-form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String createAppointment(@Valid @ModelAttribute("request") AppointmentCreateRequest request,
                                    BindingResult result, Model model, HttpServletRequest req) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("doctors", doctorRepository.findAll());
            addUserInfo(model, req);
            return "appointment-form";
        }
        appointmentService.createAppointment(request);
        return "redirect:/web/appointments";
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String showDoctorSearch(Model model, HttpServletRequest request) {
        model.addAttribute("doctors", doctorRepository.findAll());
        addUserInfo(model, request);
        return "doctor-appointments";
    }

    @GetMapping("/doctor/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String searchByDoctor(@RequestParam String doctorPublicId, Model model, HttpServletRequest request) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDoctorPublicId(doctorPublicId);
        model.addAttribute("appointments", appointments);
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("searchedDoctorPublicId", doctorPublicId);
        addUserInfo(model, request);
        return "doctor-appointments";
    }

    @GetMapping("/patient")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String showPatientSearch(Model model, HttpServletRequest request) {
        model.addAttribute("patients", patientRepository.findAll());
        addUserInfo(model, request);
        return "patient-appointments";
    }

    @GetMapping("/patient/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String searchByPatient(@RequestParam String patientPublicId, Model model, HttpServletRequest request) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByPatientPublicId(patientPublicId);
        model.addAttribute("appointments", appointments);
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("searchedPatientPublicId", patientPublicId);
        addUserInfo(model, request);
        return "patient-appointments";
    }

    private void addUserInfo(Model model, HttpServletRequest request) {
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
}
