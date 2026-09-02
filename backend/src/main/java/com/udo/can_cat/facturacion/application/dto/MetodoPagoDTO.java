package com.udo.can_cat.facturacion.application.dto;

import java.util.Map;

public record MetodoPagoDTO(
        Integer id,
        String nombre,
        String descripcion,
        Map<String, String> camposRequeridos
) {}