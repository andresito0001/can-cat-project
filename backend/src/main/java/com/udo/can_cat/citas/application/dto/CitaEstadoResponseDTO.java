package com.udo.can_cat.citas.application.dto;

public record CitaEstadoResponseDTO(
        Integer idCita,
        String estado,
        String mensaje
) {}