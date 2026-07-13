package com.udo.can_cat.usuarios.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record PersonalDTO (
    Integer personalId,     
    Integer usuarioId, 
    String codigoEmpleado,
    String cargo,
    String especialidad,
    LocalDate fechaContratacion,
    Boolean activo,
    Map<String, Object> horarioAtencion,
    String licenciaProfesional,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}