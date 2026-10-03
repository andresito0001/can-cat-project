package com.udo.can_cat.usuarios.application.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ActualizarClienteRequestDTO(
    @NotBlank @Size(min = 3, max = 150) String nombreCompleto,
    
    @NotBlank @Size(min = 6, max = 20) 
    @Pattern(
        regexp = com.udo.can_cat.shared.validation.DocumentoIdentidad.PATTERN,
        message = com.udo.can_cat.shared.validation.DocumentoIdentidad.MENSAJE
    )
    
    String documentoIdentidad,
    @NotBlank @Pattern(regexp = "^[0-9+\\-() ]{7,20}$") String telefonoPrincipal,
    @Pattern(regexp = "^[0-9+\\-() ]{7,20}$") String telefonoSecundario,
    String direccion,
    String ciudad,
    LocalDate fechaNacimiento
) {}