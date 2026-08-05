package com.dnt.catalog.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
    @NotNull(message = "El estado es obligatorio")
    Boolean active
) {}
