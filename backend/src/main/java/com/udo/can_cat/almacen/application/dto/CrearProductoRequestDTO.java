package com.udo.can_cat.almacen.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearProductoRequestDTO(
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 50) String codigoSku,        
        @Size(max = 255) String descripcion,
        @NotBlank String categoria,
        @NotBlank String presentacion,
        @NotNull @DecimalMin("0") BigDecimal precioVenta,
        @DecimalMin("0") BigDecimal costoAdquisicion,
        @NotNull @Min(0) Integer stockActual,
        @NotNull @Min(0) Integer stockMinimo,
        @Min(0) Integer stockMaximo,
        Boolean requiereReceta,
        Integer idProveedorPredeterminado
) {}