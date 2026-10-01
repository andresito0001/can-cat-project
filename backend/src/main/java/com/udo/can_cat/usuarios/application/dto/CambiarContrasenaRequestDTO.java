package com.udo.can_cat.usuarios.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cambio de contraseña desde sesión activa (usuario autenticado).
 * Requiere verificar la contraseña actual.
 */
public record CambiarContrasenaRequestDTO(

        @NotBlank(message = "La contraseña actual es obligatoria")
        String contrasenaActual,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres")
        String contrasenaNueva
) {}