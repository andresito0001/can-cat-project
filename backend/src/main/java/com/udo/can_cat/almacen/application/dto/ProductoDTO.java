package com.udo.can_cat.almacen.application.dto;

import java.math.BigDecimal;

/** 6.2. precioUsd = producto.precio_venta (USD, convención 1.3). */
public record ProductoDTO(
        Integer id,
        String codigoSku,
        String nombre,
        String tipoCategoria,
        String presentacion,
        BigDecimal precioUsd,
        Integer stockActual,
        Boolean requiereReceta) {}