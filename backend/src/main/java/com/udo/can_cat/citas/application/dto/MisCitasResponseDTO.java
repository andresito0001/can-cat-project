package com.udo.can_cat.citas.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MisCitasResponseDTO(
    Integer idCita,
    String nombreMascota,
    String nombreVeterinario,
    String nombreServicio,
    String estado,
    String colorEstado,
    String fechaCita,
    String horaInicio,
    String horaFin,
    BigDecimal costoUsd,
    BigDecimal costoBs,
    BigDecimal subtotalUsd,
    BigDecimal ivaUsd,
    BigDecimal porcentajeIva,
    LocalDateTime expiraEn,
    String estadoPago
) {}