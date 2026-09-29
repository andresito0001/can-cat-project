package com.udo.can_cat.mascotas.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarEstadoMascotaRequestDTO(
        @NotBlank
        @Pattern(regexp = "^(Activa|Inactiva|Fallecida)$",
                 message = "El estado debe ser Activa, Inactiva o Fallecida")
        String estado
) {}