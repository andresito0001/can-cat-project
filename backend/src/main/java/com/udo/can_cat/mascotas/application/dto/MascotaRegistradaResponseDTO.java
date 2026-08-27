package com.udo.can_cat.mascotas.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MascotaRegistradaResponseDTO(
        Integer idMascota,
        Integer idCliente,
        String nombreCliente,
        Integer idEspecie,
        String nombreEspecie,
        Integer idRaza,
        String nombreRaza,
        String nombre,
        LocalDate fechaNacimiento,
        String sexo,
        String color,
        BigDecimal pesoActual,
        Boolean esterilizado,
        Boolean activo,
        LocalDateTime registradoEn
) {}