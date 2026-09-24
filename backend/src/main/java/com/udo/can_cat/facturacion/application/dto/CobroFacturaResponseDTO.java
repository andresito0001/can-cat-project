package com.udo.can_cat.facturacion.application.dto;

import java.math.BigDecimal;

public record CobroFacturaResponseDTO(
        Integer idFactura,
        String numeroControl,
        BigDecimal monto,
        String metodoPago,
        String estadoPago,
        String mensaje) {}