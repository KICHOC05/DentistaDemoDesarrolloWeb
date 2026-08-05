package com.dnt.catalog.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateDentalServiceRequest {

    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal price;

    @Min(value = 1, message = "La duracion debe ser mayor a 0")
    private Integer durationMinutes;
}
