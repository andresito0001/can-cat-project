package com.udo.can_cat.citas.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarEstadoCitaRequestDTO(
        @NotBlank(message = "El estado destino es obligatorio")
        String estado
) {}