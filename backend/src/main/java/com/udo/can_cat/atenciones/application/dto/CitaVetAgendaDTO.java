package com.udo.can_cat.atenciones.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaVetAgendaDTO(
        Integer idCita,
        String mascotaNombre, String especie, String raza,
        String clienteNombre, String clienteDocumento, String clienteTelefono,
        String motivoConsulta, String servicioNombre,
        LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin,
        String estado, String estadoColor,
        boolean atendida) {}