package com.udo.can_cat.citas.application.dto;

import java.math.BigDecimal;

/** CU 4.6.1.11 paso 2: turno del día para el calendario general. */
public record CitaAgendaDTO(
        Integer idCita,
        Integer idVeterinario,
        String nombreVeterinario,
        Integer idCliente,
        String nombreCliente,
        String documentoCliente,
        Integer idMascota,
        String nombreMascota,
        String nombreServicio,
        String motivoConsulta,
        String fecha,
        String horaInicio,
        String horaFin,
        String estado,
        String colorUi,
        BigDecimal costoUsd,
        BigDecimal costoBs
) {}