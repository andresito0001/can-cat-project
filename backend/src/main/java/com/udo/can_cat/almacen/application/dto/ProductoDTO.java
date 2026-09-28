package com.udo.can_cat.almacen.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoDTO(
        Integer id,
        String codigoSku,
        String nombre,
        String descripcion,
        String tipoCategoria,
        Integer idCategoria,
        String presentacion,
        BigDecimal precioUsd,
        BigDecimal costoAdquisicion,
        Integer stockActual,
        Integer stockMinimo,
        Integer stockMaximo,
        Boolean requiereReceta,
        Boolean activo,
        Integer idProveedorPredeterminado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}