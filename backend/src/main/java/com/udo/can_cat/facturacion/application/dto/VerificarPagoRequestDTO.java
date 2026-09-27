package com.udo.can_cat.facturacion.application.dto;

import jakarta.validation.constraints.NotNull;

public record VerificarPagoRequestDTO(
        @NotNull(message = "Debe indicar si aprueba o rechaza")
        Boolean aprobado,
        String observaciones
) {}