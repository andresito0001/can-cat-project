package com.udo.can_cat.usuarios.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Solo los Clientes pueden actualizar sus datos desde la vista Mi Perfil.
 * El documento de identidad NO se edita aquí (requiere gestión de recepción).
 */
public record ActualizarPerfilRequestDTO(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El teléfono principal es obligatorio")
        @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "El formato del teléfono no es válido")
        String telefonoPrincipal,

        @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "El formato del teléfono no es válido")
        String telefonoSecundario,

        String direccion,

        @Size(max = 50)
        String ciudad,

        LocalDate fechaNacimiento
) {}