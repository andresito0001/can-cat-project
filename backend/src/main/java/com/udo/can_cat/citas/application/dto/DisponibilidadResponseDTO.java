package com.udo.can_cat.citas.application.dto;

import java.util.List;

public record DisponibilidadResponseDTO(
        Integer idVeterinario,
        String nombreVeterinario,
        String fecha,
        String servicio,
        Integer duracionMinutos,
        List<BloqueHorarioDTO> bloquesDisponibles
) {}