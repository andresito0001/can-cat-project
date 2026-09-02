package com.udo.can_cat.citas.application.dto;

import java.math.BigDecimal;

public record ServicioDTO(
        Integer id,
        String nombre,
        String tipoAtencion,
        String descripcion,
        Integer duracionMinutos,
        BigDecimal precioUsd
) {}