package com.udo.can_cat.citas.application.dto;

import java.math.BigDecimal;

public record AgendarMostradorResponseDTO(
        Integer idCita,
        String estado,
        Integer idFactura,
        String numeroControl,
        String mensaje,
        ResumenMostradorDTO resumen
) {
    public record ResumenMostradorDTO (
            String cliente,
            String documentoCliente,
            String mascota,
            String veterinario,
            String servicio,
            String fecha,
            String horaInicio,
            String horaFin,
            BigDecimal costoUsd,
            BigDecimal costoBs,
            BigDecimal tasaCambio,
            String metodoPago
    ) {}
}