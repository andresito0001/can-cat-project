package com.udo.can_cat.usuarios.application.dto;

import com.udo.can_cat.usuarios.domain.entity.Personal.Cargo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

public record ActualizarPersonalRequestDTO(
    @NotBlank String nombreCompleto,
    @NotBlank String codigoEmpleado,
    @NotNull Cargo cargo,
    String especialidad,
    @NotNull LocalDate fechaContratacion,
    String licenciaProfesional,
    Map<String, Object> horarioAtencion   
) {}