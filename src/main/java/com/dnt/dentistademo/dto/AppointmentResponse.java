package com.dnt.dentistademo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private String publicId;
    private String patientName;
    private String doctorName;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private String reason;
    private String status;

    public String getStatusClass() {
        if (status == null) return "";
        return switch (status) {
            case "PENDING" -> "status-pending";
            case "CONFIRMED" -> "status-confirmed";
            case "COMPLETED" -> "status-completed";
            case "CANCELLED" -> "status-cancelled";
            default -> "";
        };
    }
}
