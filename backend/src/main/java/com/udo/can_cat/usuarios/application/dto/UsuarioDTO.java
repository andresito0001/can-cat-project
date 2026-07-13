package com.udo.can_cat.usuarios.application.dto;

import java.time.LocalDateTime;

public record UsuarioDTO (
    Integer id,
    Integer rolId,
    String correoElectronico,
    String contrasena,
    String estado,
    LocalDateTime fechaRegistro,
    LocalDateTime ultimoAcceso
) {}