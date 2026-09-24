package com.udo.can_cat.almacen.application.dto;

public record CategoriaProductoDTO(
        Integer id,
        String nombre,
        String descripcion,
        Boolean requierePrescripcion
) {}
