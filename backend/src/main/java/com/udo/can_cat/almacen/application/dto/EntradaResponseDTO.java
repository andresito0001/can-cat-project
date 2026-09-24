package com.udo.can_cat.almacen.application.dto;

import java.math.BigDecimal;

public record EntradaResponseDTO(
        Integer idCompra,
        String numeroOrden,
        Integer totalProductos,
        Integer totalUnidades,
        BigDecimal montoTotal,
        String mensaje   // "Inventario actualizado correctamente."
) {}
