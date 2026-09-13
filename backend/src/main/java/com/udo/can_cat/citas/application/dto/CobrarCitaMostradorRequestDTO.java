package com.udo.can_cat.citas.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record CobrarCitaMostradorRequestDTO(
        @NotNull(message = "El método de pago es obligatorio")
        Integer idMetodoPago,

        @Size(max = 100, message = "La referencia no puede exceder 100 caracteres")
        String referenciaTransaccion,

        Map<String, String> datosPago
) {}