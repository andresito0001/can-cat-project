package com.udo.can_cat.almacen.application.dto;

public record ProveedorDTO(
        Integer id,
        String rif,
        String nombreEmpresa,
        String nombreContacto,
        String telefono,
        String correo,
        String tipoSuministro,
        Boolean activo
) {}
