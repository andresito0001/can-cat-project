package com.udo.can_cat.mascotas.application.dto;

public record RazaDTO(
        Integer id,
        Integer idEspecie,
        String nombre,
        String caracteristicas
) {}