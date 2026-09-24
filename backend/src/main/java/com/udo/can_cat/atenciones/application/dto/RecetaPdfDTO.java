package com.udo.can_cat.atenciones.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecetaPdfDTO(
        Integer idReceta,
        String codigoReceta,
        LocalDateTime fechaEmision,
        LocalDateTime fechaAtencion,
        String diagnostico,
        String indicacionesGenerales,
        String veterinarioNombre,
        String veterinarioEspecialidad,
        String veterinarioLicencia,
        String mascotaNombre,
        String mascotaEspecie,
        String mascotaRaza,
        String mascotaSexo,
        String clienteNombre,
        String clienteDocumento,
        List<ItemPdfDTO> items) {

    public record ItemPdfDTO(
            Integer orden,
            String medicamento,
            String concentracion,
            String dosis,
            String viaAdministracion,
            String frecuencia,
            String duracion) {}
}