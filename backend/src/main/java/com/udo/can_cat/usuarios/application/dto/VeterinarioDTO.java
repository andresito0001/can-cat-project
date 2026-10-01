package com.udo.can_cat.usuarios.application.dto;

import java.util.Map;

public record VeterinarioDTO(
    Integer id,
    String nombre,
    String especialidad,
    Map<String, Object> horarioAtencion
) {}