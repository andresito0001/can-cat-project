package com.udo.can_cat.citas.application.dto;

import java.math.BigDecimal;

public record ResumenCitaDTO(
        String mascota,
        String veterinario,
        String servicio,
        String fecha,
        String horaInicio,
        String horaFin,
        BigDecimal costoUsd,
        BigDecimal costoBs,
        BigDecimal tasaCambio
) {}