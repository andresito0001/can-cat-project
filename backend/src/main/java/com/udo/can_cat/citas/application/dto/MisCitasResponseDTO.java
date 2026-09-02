package com.udo.can_cat.citas.application.dto;

import java.math.BigDecimal;

public record MisCitasResponseDTO(
        Integer idCita,
        String mascota,
        String veterinario,
        String servicio,
        String estado,
        String colorUi,
        String fechaCita,
        String horaInicio,
        String horaFin,
        BigDecimal costoUsd,
        BigDecimal costoBs
) {}