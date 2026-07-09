package com.dnt.dentistademo.web.controller;

import com.dnt.dentistademo.dto.AppointmentCreateRequest;
import com.dnt.dentistademo.dto.AppointmentResponse;
import com.dnt.dentistademo.repository.DoctorRepository;
import com.dnt.dentistademo.repository.PatientRepository;
import com.dnt.dentistademo.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/web/appointments")
@RequiredArgsConstructor
public class AppointmentViewController {

    private final AppointmentService appointmentService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping
    public String listAppointments(Model model) {
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        model.addAttribute("appointments", appointments);
        return "appointments";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("request", new AppointmentCreateRequest());
        return "appointment-form";
    }

    @PostMapping
    public String createAppointment(@Valid @ModelAttribute("request") AppointmentCreateRequest request,
                                    BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("doctors", doctorRepository.findAll());
            return "appointment-form";
        }
        appointmentService.createAppointment(request);
        return "redirect:/web/appointments";
    }

    @GetMapping("/doctor")
    public String showDoctorSearch(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "doctor-appointments";
    }

    @GetMapping("/doctor/results")
    public String searchByDoctor(@RequestParam String doctorPublicId, Model model) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDoctorPublicId(doctorPublicId);
        model.addAttribute("appointments", appointments);
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("searchedDoctorPublicId", doctorPublicId);
        return "doctor-appointments";
    }

    @GetMapping("/patient")
    public String showPatientSearch(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        return "patient-appointments";
    }

    @GetMapping("/patient/results")
    public String searchByPatient(@RequestParam String patientPublicId, Model model) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByPatientPublicId(patientPublicId);
        model.addAttribute("appointments", appointments);
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("searchedPatientPublicId", patientPublicId);
        return "patient-appointments";
    }
}
