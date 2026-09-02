package com.udo.can_cat.facturacion.application.dto;

public record ProcesarPagoCitaResponseDTO(
        Integer idFactura,
        String numeroControl,
        String estadoCita,
        String mensaje,
        boolean emailEnviado,
        String advertenciaEmail
) {}