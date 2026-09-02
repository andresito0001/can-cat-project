package com.udo.can_cat.facturacion.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ProcesarPagoCitaRequestDTO(
        @NotNull(message = "La cita es obligatoria")
        Integer idCita,

        @NotNull(message = "El método de pago es obligatorio")
        Integer idMetodoPago,

        @NotBlank(message = "La referencia de transacción es obligatoria")
        String referenciaTransaccion,

        Map<String, String> datosPago
) {}