package com.udo.can_cat.facturacion.application.dto;

import java.math.BigDecimal;

public record HistorialPagoResponseDTO(
        Integer idFactura,
        String numeroControl,
        String fecha,
        String concepto,            
        BigDecimal monto,           
        String estadoPago,
        String metodoPago,
        String referenciaTransaccion,
        Integer idCita
) {}