package com.udo.can_cat.mascotas.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarMascotaRequestDTO (
        String documentoIdentidadCliente,

        @NotNull(message = "La especie es obligatoria")
        Integer idEspecie,

        Integer idRaza,

        @NotBlank(message = "El nombre de la mascota es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        String nombre,

        LocalDate fechaNacimiento,

        @NotNull(message = "El sexo es obligatorio")
        @Pattern(regexp = "^[MH]$", message = "El sexo debe ser 'M' o 'H'")
        String sexo,

        @Size(max = 30, message = "El color no puede exceder 30 caracteres")
        String color,

        @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
        @DecimalMax(value = "999.99", message = "El peso no puede exceder 999.99")
        @Digits(integer = 3, fraction = 2, message = "Formato de peso inválido (máx 3 enteros, 2 decimales)")
        BigDecimal pesoActual,

        Boolean esterilizado
) {}