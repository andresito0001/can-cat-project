package com.udo.can_cat.facturacion.application.dto;

public record EnvioFacturaResponseDTO(
        boolean enviado,
        String mensaje
) {}