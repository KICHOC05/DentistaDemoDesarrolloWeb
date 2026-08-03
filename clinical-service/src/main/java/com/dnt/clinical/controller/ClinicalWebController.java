package com.dnt.clinical.controller;

import com.dnt.clinical.dto.ClinicalRecordRequest;
import com.dnt.clinical.dto.DiagnosisRequest;
import com.dnt.clinical.dto.TreatmentRequest;
import com.dnt.clinical.repository.PatientRepository;
import com.dnt.clinical.service.ClinicalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/clinical")
@RequiredArgsConstructor
public class ClinicalWebController {

    private final ClinicalService clinicalService;
    private final PatientRepository patientRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String index(Model model, HttpServletRequest request) {
        model.addAttribute("patients", patientRepository.findAll());
        addUserInfo(model, request);
        return "clinical-index";
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String search(@RequestParam("q") String query, Model model, HttpServletRequest request) {
        var patients = patientRepository.findByFullNameContainingIgnoreCase(query);
        if (patients.size() == 1) {
            return "redirect:/web/clinical/" + patients.get(0).getPublicId();
        }
        model.addAttribute("patients", patients);
        model.addAttribute("q", query);
        addUserInfo(model, request);
        return "clinical-index";
    }

    @GetMapping("/{patientPublicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String viewRecord(@PathVariable String patientPublicId,
                              Model model, HttpServletRequest request) {
        var patient = patientRepository.findByPublicId(patientPublicId).orElse(null);
        var record = clinicalService.getRecordByPatient(patientPublicId);
        var diagnoses = clinicalService.getDiagnoses(patientPublicId);
        var treatments = clinicalService.getTreatments(patientPublicId);

        model.addAttribute("patient", patient);
        model.addAttribute("record", record);
        model.addAttribute("diagnoses", diagnoses);
        model.addAttribute("treatments", treatments);
        model.addAttribute("patientPublicId", patientPublicId);
        model.addAttribute("diagnosisRequest", new DiagnosisRequest());
        model.addAttribute("treatmentRequest", new TreatmentRequest());
        addUserInfo(model, request);
        return "clinical-record";
    }

    @PostMapping("/{patientPublicId}/record")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String updateRecord(@PathVariable String patientPublicId,
                                @ModelAttribute ClinicalRecordRequest recordRequest) {
        recordRequest.setPatientPublicId(patientPublicId);
        clinicalService.getOrCreateRecord(recordRequest);
        return "redirect:/web/clinical/" + patientPublicId;
    }

    @PostMapping("/{patientPublicId}/diagnoses")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String addDiagnosis(@PathVariable String patientPublicId,
                                @Valid @ModelAttribute("diagnosisRequest") DiagnosisRequest request,
                                BindingResult result, Model model, HttpServletRequest req) {
        if (result.hasErrors()) {
            model.addAttribute("patient", patientRepository.findByPublicId(patientPublicId).orElse(null));
            model.addAttribute("record", clinicalService.getRecordByPatient(patientPublicId));
            model.addAttribute("diagnoses", clinicalService.getDiagnoses(patientPublicId));
            model.addAttribute("treatments", clinicalService.getTreatments(patientPublicId));
            model.addAttribute("patientPublicId", patientPublicId);
            model.addAttribute("treatmentRequest", new TreatmentRequest());
            addUserInfo(model, req);
            return "clinical-record";
        }
        request.setPatientPublicId(patientPublicId);
        if (request.getDiagnosedDate() == null) {
            request.setDiagnosedDate(LocalDate.now());
        }
        clinicalService.addDiagnosis(request);
        return "redirect:/web/clinical/" + patientPublicId;
    }

    @PostMapping("/{patientPublicId}/treatments")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String addTreatment(@PathVariable String patientPublicId,
                                @Valid @ModelAttribute("treatmentRequest") TreatmentRequest request,
                                BindingResult result, Model model, HttpServletRequest req) {
        if (result.hasErrors()) {
            model.addAttribute("patient", patientRepository.findByPublicId(patientPublicId).orElse(null));
            model.addAttribute("record", clinicalService.getRecordByPatient(patientPublicId));
            model.addAttribute("diagnoses", clinicalService.getDiagnoses(patientPublicId));
            model.addAttribute("treatments", clinicalService.getTreatments(patientPublicId));
            model.addAttribute("patientPublicId", patientPublicId);
            model.addAttribute("diagnosisRequest", new DiagnosisRequest());
            addUserInfo(model, req);
            return "clinical-record";
        }
        request.setPatientPublicId(patientPublicId);
        clinicalService.addTreatment(request);
        return "redirect:/web/clinical/" + patientPublicId;
    }

    @GetMapping("/{patientPublicId}/odontogram")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public String viewOdontogram(@PathVariable String patientPublicId,
                                  Model model, HttpServletRequest request) {
        var teeth = clinicalService.getOdontogram(patientPublicId);
        Map<Integer, String> toothMap = teeth.stream()
                .collect(Collectors.toMap(
                        t -> t.getToothNumber(),
                        t -> t.getCondition()
                ));

        List<Integer> upperRight = Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18);
        List<Integer> upperLeft = Arrays.asList(21, 22, 23, 24, 25, 26, 27, 28);
        List<Integer> lowerLeft = Arrays.asList(31, 32, 33, 34, 35, 36, 37, 38);
        List<Integer> lowerRight = Arrays.asList(41, 42, 43, 44, 45, 46, 47, 48);

        model.addAttribute("toothMap", toothMap);
        model.addAttribute("upperRight", upperRight);
        model.addAttribute("upperLeft", upperLeft);
        model.addAttribute("lowerLeft", lowerLeft);
        model.addAttribute("lowerRight", lowerRight);
        model.addAttribute("patientPublicId", patientPublicId);
        model.addAttribute("conditions", List.of(
                "SANO", "CARIADO", "OBTURADO", "ENDODONCIADO",
                "AUSENTE", "PROTESIS", "CORONA", "FRACTURADO"));
        addUserInfo(model, request);
        return "odontogram";
    }

    private void addUserInfo(Model model, HttpServletRequest request) {
        String token = extractToken(request);
        model.addAttribute("token", token != null ? token : "");
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
