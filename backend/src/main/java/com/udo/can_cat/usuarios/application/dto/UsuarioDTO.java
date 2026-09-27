package com.udo.can_cat.usuarios.application.dto;

import java.time.LocalDateTime;

public record UsuarioDTO(
    Integer id,
    Integer rolId,
    String correoElectronico,
    String contrasena,
    String estado,
    LocalDateTime fechaRegistro,
    LocalDateTime ultimoAcceso,
    String rolNombre,
    String nombreCompleto,
    String documentoIdentidad,
    String tipoUsuario,   
    Integer entidadId          
) {
    public static UsuarioDTO paraCrear(
        Integer rolId, String correo, String contrasena,
        String rolNombre, String nombreCompleto, String tipoUsuario) {
    return new UsuarioDTO(
        null, rolId, correo, contrasena,
        "Activo", LocalDateTime.now(), null,
        rolNombre, nombreCompleto,
        null, tipoUsuario, null
    );
}
}