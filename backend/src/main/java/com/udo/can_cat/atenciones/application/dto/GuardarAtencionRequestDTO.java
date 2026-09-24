package com.udo.can_cat.atenciones.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * D6: validación backend (alterno C) → 400 vía MethodArgumentNotValidException.
 * Rangos espejo de los CHECKs de V7. La receta es OPCIONAL (nullable), pero si
 * viene, sus ítems no pueden estar vacíos.
 */
public record GuardarAtencionRequestDTO(

        @NotNull(message = "La cita es obligatoria")
        @Positive
        Integer idCita,

        @NotBlank(message = "La anamnesis es obligatoria")
        String anamnesis,

        String sintomasObservados,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "0.01", message = "El peso debe estar entre 0.01 y 999.99 kg")
        @DecimalMax(value = "999.99", message = "El peso debe estar entre 0.01 y 999.99 kg")
        @Digits(integer = 3, fraction = 2)
        BigDecimal pesoKg,

        @NotNull(message = "La temperatura es obligatoria")
        @DecimalMin(value = "30.0", message = "La temperatura debe estar entre 30.0 y 45.0 °C")
        @DecimalMax(value = "45.0", message = "La temperatura debe estar entre 30.0 y 45.0 °C")
        @Digits(integer = 2, fraction = 1)
        BigDecimal temperaturaC,

        @NotNull(message = "La frecuencia cardíaca es obligatoria")
        @Min(value = 20, message = "La frecuencia cardíaca debe estar entre 20 y 400 lpm")
        @Max(value = 400, message = "La frecuencia cardíaca debe estar entre 20 y 400 lpm")
        Integer frecCardiaca,

        @Min(value = 1, message = "La frecuencia respiratoria debe estar entre 1 y 400 rpm")
        @Max(value = 400, message = "La frecuencia respiratoria debe estar entre 1 y 400 rpm")
        Integer frecRespiratoria,

        @NotBlank(message = "El diagnóstico es obligatorio")
        String diagnosticoPrincipal,

        String diagnosticosDiferenciales,

        String observacionesGenerales,

        String indicacionesDueno,

        LocalDate proximaCitaRecomendada,

        @Valid
        List<InsumoRequestDTO> insumos,

        @Valid
        RecetaRequestDTO receta
) {

    public record InsumoRequestDTO(
            @NotNull(message = "El producto del insumo es obligatorio")
            @Positive
            Integer idProducto,

            @NotNull(message = "La cantidad del insumo es obligatoria")
            @Min(value = 1, message = "La cantidad mínima es 1")
            Integer cantidad) {}

    public record RecetaRequestDTO(
            String indicacionesGenerales,
            @NotEmpty(message = "El récipe debe tener al menos un ítem")
            @Valid
            List<ItemRequestDTO> items) {}

    public record ItemRequestDTO(
            @NotBlank(message = "El medicamento es obligatorio")
            @Size(max = 150)
            String medicamento,

            @Size(max = 100)
            String concentracion,

            @NotBlank(message = "La dosis es obligatoria")
            @Size(max = 100)
            String dosis,

            @Size(max = 50)
            String viaAdministracion,

            @NotBlank(message = "La frecuencia es obligatoria")
            @Size(max = 100)
            String frecuencia,

            @NotBlank(message = "La duración es obligatoria")
            @Size(max = 100)
            String duracion) {}
}