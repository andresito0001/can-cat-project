package com.udo.can_cat.atenciones.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** D8/D11: registro completo para el historial clínico y el modo solo-lectura del contexto. */
public record AtencionHistorialDTO(
        Integer idAtencion, Integer idCita,
        LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String estadoAtencion,
        String veterinarioNombre,
        String anamnesis, String sintomasObservados,
        BigDecimal pesoKg, BigDecimal temperaturaC, Integer frecCardiaca, Integer frecRespiratoria,
        String diagnosticoPrincipal, String diagnosticosDiferenciales,
        String observacionesGenerales, String indicacionesDueno, LocalDate proximaCitaRecomendada,
        List<InsumoDTO> insumos,
        RecetaDTO receta) {

    public record InsumoDTO(Integer idProducto, String nombre, String codigoSku, String unidadMedida,
                            Integer cantidad, BigDecimal precioUnitarioUsd, BigDecimal subtotalUsd) {}

    public record RecetaDTO(Integer idReceta, String codigoReceta, String indicacionesGenerales,
                            LocalDateTime fechaEmision, List<ItemDTO> items) {}

    public record ItemDTO(String medicamento, String concentracion, String dosis, String viaAdministracion,
                          String frecuencia, String duracion, Integer orden) {}
}