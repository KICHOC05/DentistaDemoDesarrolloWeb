package com.dnt.clinical.service;

import com.dnt.clinical.dto.ClinicalFileResponse;
import com.dnt.clinical.dto.ClinicalRecordRequest;
import com.dnt.clinical.dto.ClinicalRecordResponse;
import com.dnt.clinical.dto.DiagnosisRequest;
import com.dnt.clinical.dto.DiagnosisResponse;
import com.dnt.clinical.dto.OdontogramRequest;
import com.dnt.clinical.dto.OdontogramResponse;
import com.dnt.clinical.dto.TreatmentRequest;
import com.dnt.clinical.dto.TreatmentResponse;
import com.dnt.clinical.model.ClinicalFile;
import com.dnt.clinical.model.ClinicalRecord;
import com.dnt.clinical.model.Diagnosis;
import com.dnt.clinical.model.Odontogram;
import com.dnt.clinical.model.ToothCondition;
import com.dnt.clinical.model.Treatment;
import com.dnt.clinical.model.TreatmentStatus;
import com.dnt.clinical.repository.ClinicalFileRepository;
import com.dnt.clinical.repository.ClinicalRecordRepository;
import com.dnt.clinical.repository.DiagnosisRepository;
import com.dnt.clinical.repository.OdontogramRepository;
import com.dnt.clinical.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicalServiceImpl implements ClinicalService {

    private final ClinicalRecordRepository recordRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final TreatmentRepository treatmentRepository;
    private final OdontogramRepository odontogramRepository;
    private final ClinicalFileRepository fileRepository;

    @Override
    @Transactional
    public ClinicalRecordResponse getOrCreateRecord(ClinicalRecordRequest request) {
        ClinicalRecord record = recordRepository.findByPatientPublicId(request.getPatientPublicId())
                .orElseGet(() -> ClinicalRecord.builder()
                        .patientPublicId(request.getPatientPublicId())
                        .build());
        record.setBloodType(request.getBloodType());
        record.setAllergies(request.getAllergies());
        record.setChronicConditions(request.getChronicConditions());
        record.setMedicalNotes(request.getMedicalNotes());
        record = recordRepository.save(record);
        return toRecordResponse(record);
    }

    @Override
    public ClinicalRecordResponse getRecordByPatient(String patientPublicId) {
        ClinicalRecord record = recordRepository.findByPatientPublicId(patientPublicId)
                .orElse(null);
        if (record == null) return null;
        return toRecordResponse(record);
    }

    @Override
    public List<DiagnosisResponse> getDiagnoses(String patientPublicId) {
        return diagnosisRepository.findByPatientPublicIdOrderByDiagnosedDateDesc(patientPublicId)
                .stream().map(this::toDiagnosisResponse).toList();
    }

    @Override
    public DiagnosisResponse addDiagnosis(DiagnosisRequest request) {
        Diagnosis diagnosis = Diagnosis.builder()
                .patientPublicId(request.getPatientPublicId())
                .diagnosisCode(request.getDiagnosisCode())
                .description(request.getDescription())
                .diagnosedBy(request.getDiagnosedBy())
                .diagnosedDate(request.getDiagnosedDate() != null ? request.getDiagnosedDate() : LocalDate.now())
                .notes(request.getNotes())
                .build();
        diagnosis = diagnosisRepository.save(diagnosis);
        return toDiagnosisResponse(diagnosis);
    }

    @Override
    public List<TreatmentResponse> getTreatments(String patientPublicId) {
        return treatmentRepository.findByPatientPublicIdOrderByStartDateDesc(patientPublicId)
                .stream().map(this::toTreatmentResponse).toList();
    }

    @Override
    public TreatmentResponse addTreatment(TreatmentRequest request) {
        Treatment treatment = Treatment.builder()
                .patientPublicId(request.getPatientPublicId())
                .toothNumber(request.getToothNumber())
                .treatmentType(request.getTreatmentType())
                .description(request.getDescription())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .endDate(request.getEndDate())
                .cost(request.getCost())
                .notes(request.getNotes())
                .build();
        treatment = treatmentRepository.save(treatment);
        return toTreatmentResponse(treatment);
    }

    @Override
    public TreatmentResponse updateTreatmentStatus(Long id, String status) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tratamiento no encontrado"));
        TreatmentStatus currentStatus = treatment.getStatus();
        TreatmentStatus newStatus = TreatmentStatus.valueOf(status);
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                "No se puede cambiar el estado de " + currentStatus + " a " + newStatus);
        }
        treatment.setStatus(newStatus);
        treatment = treatmentRepository.save(treatment);
        return toTreatmentResponse(treatment);
    }

    @Override
    public List<OdontogramResponse> getOdontogram(String patientPublicId) {
        return odontogramRepository.findByPatientPublicId(patientPublicId)
                .stream().map(this::toOdontogramResponse).toList();
    }

    @Override
    public OdontogramResponse updateTooth(OdontogramRequest request) {
        Odontogram tooth = odontogramRepository
                .findByPatientPublicIdAndToothNumber(request.getPatientPublicId(), request.getToothNumber())
                .orElseGet(() -> Odontogram.builder()
                        .patientPublicId(request.getPatientPublicId())
                        .toothNumber(request.getToothNumber())
                        .build());
        tooth.setCondition(ToothCondition.valueOf(request.getCondition()));
        tooth.setNotes(request.getNotes());
        tooth = odontogramRepository.save(tooth);
        return toOdontogramResponse(tooth);
    }

    @Override
    public List<ClinicalFileResponse> getFiles(String patientPublicId) {
        return fileRepository.findByPatientPublicIdOrderByUploadedAtDesc(patientPublicId)
                .stream().map(this::toFileResponse).toList();
    }

    @Override
    public ClinicalFileResponse addFile(String patientPublicId, String fileName,
                                         String contentType, String data, String description) {
        ClinicalFile file = ClinicalFile.builder()
                .patientPublicId(patientPublicId)
                .fileName(fileName)
                .contentType(contentType)
                .data(data)
                .description(description)
                .build();
        file = fileRepository.save(file);
        return toFileResponse(file);
    }

    private ClinicalRecordResponse toRecordResponse(ClinicalRecord r) {
        return ClinicalRecordResponse.builder()
                .patientPublicId(r.getPatientPublicId())
                .bloodType(r.getBloodType())
                .allergies(r.getAllergies())
                .chronicConditions(r.getChronicConditions())
                .medicalNotes(r.getMedicalNotes())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private DiagnosisResponse toDiagnosisResponse(Diagnosis d) {
        return DiagnosisResponse.builder()
                .id(d.getId()).patientPublicId(d.getPatientPublicId())
                .diagnosisCode(d.getDiagnosisCode()).description(d.getDescription())
                .diagnosedBy(d.getDiagnosedBy()).diagnosedDate(d.getDiagnosedDate())
                .notes(d.getNotes()).createdAt(d.getCreatedAt()).build();
    }

    private TreatmentResponse toTreatmentResponse(Treatment t) {
        return TreatmentResponse.builder()
                .id(t.getId()).patientPublicId(t.getPatientPublicId())
                .toothNumber(t.getToothNumber()).treatmentType(t.getTreatmentType())
                .description(t.getDescription()).status(t.getStatus().name())
                .startDate(t.getStartDate()).endDate(t.getEndDate())
                .cost(t.getCost()).notes(t.getNotes()).createdAt(t.getCreatedAt()).build();
    }

    private OdontogramResponse toOdontogramResponse(Odontogram o) {
        return OdontogramResponse.builder()
                .id(o.getId()).patientPublicId(o.getPatientPublicId())
                .toothNumber(o.getToothNumber()).condition(o.getCondition().name())
                .notes(o.getNotes()).updatedAt(o.getUpdatedAt()).build();
    }

    private ClinicalFileResponse toFileResponse(ClinicalFile f) {
        return ClinicalFileResponse.builder()
                .id(f.getId()).patientPublicId(f.getPatientPublicId())
                .fileName(f.getFileName()).contentType(f.getContentType())
                .description(f.getDescription()).uploadedAt(f.getUploadedAt()).build();
    }
}
