package com.udo.can_cat.mascotas.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ActualizarMascotaRequestDTO(
        @NotNull(message = "La especie es obligatoria")
        Integer idEspecie,

        Integer idRaza,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50)
        String nombre,

        LocalDate fechaNacimiento,

        @NotNull
        @Pattern(regexp = "^[MH]$", message = "El sexo debe ser 'M' o 'H'")
        String sexo,

        @Size(max = 30)
        String color,

        @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
        @DecimalMax(value = "999.99", message = "El peso no puede exceder 999.99")
        @Digits(integer = 3, fraction = 2)
        BigDecimal pesoActual,

        Boolean esterilizado
) {}