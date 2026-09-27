package com.udo.can_cat.usuarios.application.dto;

import com.udo.can_cat.usuarios.domain.entity.Personal.Cargo;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Map;

public record RegistrarPersonalRequestDTO(
    @NotBlank @Email(message = "Correo inválido") String correoElectronico,
    @NotBlank @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String contrasena,
    @NotBlank String nombreCompleto,
    String codigoEmpleado,
    @NotNull Cargo cargo,
    String especialidad,
    @NotNull LocalDate fechaContratacion,
    Map<String, Object> horarioAtencion
) {}