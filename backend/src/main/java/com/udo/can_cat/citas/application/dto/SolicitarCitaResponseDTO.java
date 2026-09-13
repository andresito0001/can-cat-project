package com.udo.can_cat.citas.application.dto;

public record SolicitarCitaResponseDTO(
        Integer idCita,
        String estado,
        String mensaje,
        ResumenCitaDTO resumen
) {}