package com.udo.can_cat.almacen.application.dto;

public record ProductoAlertaDTO(
        Integer idProducto,
        String codigoSku,
        String nombre,
        String nombreCategoria,
        Integer stockActual,
        Integer stockMinimo,
        Integer deficit,            // stock_minimo - stock_actual
        String criticidad,          // 'critica' | 'baja' | 'media'
        Boolean activo
) {}
