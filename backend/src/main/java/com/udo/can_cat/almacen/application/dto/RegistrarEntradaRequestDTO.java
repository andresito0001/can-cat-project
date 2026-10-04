package com.udo.can_cat.almacen.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RegistrarEntradaRequestDTO(
        @NotNull Integer idProveedor,
        @Size(max = 50) String numeroFactura,
        @NotNull LocalDate fechaRecepcion,
        @Size(max = 200) String observaciones,
        @NotEmpty @Valid List<LineaEntrada> lineas
) {
    public record LineaEntrada(
            @NotNull Integer idProducto,
            @NotNull @Min(1) Integer cantidadRecibida,
            @NotNull @DecimalMin("0") BigDecimal precioUnitario,
            @Size(max = 50) String numeroLote,
            LocalDate fechaVencimientoLote
    ) {}
}
