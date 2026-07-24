package com.dnt.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RescheduleRequest {
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate newDate;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime newTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime newEndTime;
}
