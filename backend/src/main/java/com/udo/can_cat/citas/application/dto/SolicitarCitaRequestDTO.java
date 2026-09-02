package com.udo.can_cat.citas.application.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitarCitaRequestDTO(

        @NotNull(message = "La mascota es obligatoria")
        Integer idMascota,

        @NotNull(message = "El veterinario es obligatorio")
        Integer idVeterinario,

        @NotNull(message = "El servicio es obligatorio")
        Integer idServicio,

        @NotNull(message = "La fecha de la cita es obligatoria")
        @Future(message = "La fecha de la cita debe ser futura")
        LocalDate fechaCita,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @NotBlank(message = "El motivo de consulta es obligatorio")
        @Size(max = 1000, message = "El motivo de consulta no puede exceder 1000 caracteres")
        String motivoConsulta

) {}