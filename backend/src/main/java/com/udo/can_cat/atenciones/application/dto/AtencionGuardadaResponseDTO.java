package com.udo.can_cat.atenciones.application.dto;

import java.math.BigDecimal;

/** F8: respuesta 201 al guardar. mensaje EXACTO del paso 9 del CU. */
public record AtencionGuardadaResponseDTO(
        Integer idAtencion,
        String codigoReceta,
        Integer idReceta,
        Integer idFacturaProductos,
        BigDecimal totalFacturaProductos,
        String mensaje,
        ResumenDTO resumen) {

    public static final String MENSAJE_EXITO = "Consulta registrada y guardada correctamente";

    public record ResumenDTO(String mascota, String diagnostico, int insumosAplicados,
                             BigDecimal totalInsumosUsd, String estadoCita) {}
}