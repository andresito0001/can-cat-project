package com.udo.can_cat.usuarios.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClienteDTO (
    Integer id,
    Integer usuarioId,
    String nombreCompleto,
    String documentoIdentidad,
    String telefonoPrincipal,
    String telefonoSecundario,
    String direccion,
    String ciudad,
    LocalDate fechaNacimiento,
    String preferenciasNotificacion,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}