package com.udo.can_cat.almacen.application.dto;

import java.time.LocalDateTime;

public record MovimientoInventarioDTO(
        Integer id,
        Integer idProducto,
        String nombreProducto,       // JOIN con producto
        String tipoMovimiento,
        String motivo,
        Integer cantidad,
        LocalDateTime fechaMovimiento,
        String documentoReferencia,
        String nombrePersonal        // JOIN con personal
) {}
