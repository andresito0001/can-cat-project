package com.udo.can_cat.almacen.application.dto;

import java.time.LocalDateTime;

public record ProveedorDTO(
        Integer id,
        String rif,
        String nombreEmpresa,
        String nombreContacto,
        String telefono,
        String correo,
        String direccion,
        String tipoSuministro,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}