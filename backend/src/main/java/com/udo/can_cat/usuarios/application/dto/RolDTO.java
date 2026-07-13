package com.udo.can_cat.usuarios.application.dto;

public record RolDTO (
    Integer id,
    String nombre,
    String descripcion,
    String permisosJs
) {}