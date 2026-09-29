package com.udo.can_cat.facturacion.application.dto;

import java.math.BigDecimal;

public record ProcesarPagoCitaResponseDTO(
        Integer idFactura,
        String numeroControl,
        String estadoCita,
        String mensaje,
        boolean emailEnviado,
        String advertenciaEmail,
        BigDecimal montoUsd,
        BigDecimal montoBs,      
        BigDecimal tasaCambio
) {}