package com.udo.can_cat.citas.application.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/**
 * CU 4.6.1.11 — Gestionar Cita (mostrador). Un solo payload atómico:
 * datos de la reserva + cobro presencial.
 */
public record AgendarMostradorRequestDTO(

        @NotNull(message = "El cliente es obligatorio")
        Integer idCliente,

        @NotNull(message = "La mascota es obligatoria")
        Integer idMascota,

        @NotNull(message = "El veterinario es obligatorio")
        Integer idVeterinario,

        @NotNull(message = "El servicio es obligatorio")
        Integer idServicio,

        @NotNull(message = "La fecha de la cita es obligatoria")
        @FutureOrPresent(message = "La fecha de la cita no puede ser pasada")
        LocalDate fechaCita,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @NotBlank(message = "El motivo de consulta es obligatorio")
        @Size(max = 1000, message = "El motivo no puede exceder 1000 caracteres")
        String motivoConsulta,

        @NotNull(message = "El método de pago es obligatorio")
        Integer idMetodoPago,

        // Opcional: Efectivo no genera referencia
        @Size(max = 100, message = "La referencia no puede exceder 100 caracteres")
        String referenciaTransaccion,

        Map<String, String> datosPago
) {}