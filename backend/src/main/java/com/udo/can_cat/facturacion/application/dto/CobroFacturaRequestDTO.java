package com.udo.can_cat.facturacion.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;

public record CobroFacturaRequestDTO(
        @NotNull(message = "El método de pago es obligatorio")
        @Positive
        Integer idMetodoPago,
        String referenciaTransaccion,
        Map<String, String> datosPago) {}