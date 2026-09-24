package com.udo.can_cat.facturacion.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FacturaPendienteDTO(
        Integer idFactura,
        String numeroControl,
        LocalDateTime fechaEmision,
        ClienteDTO cliente,
        List<DetalleDTO> detalles,
        BigDecimal totalNeto) {

    public record ClienteDTO(String nombre, String documento) {}

    public record DetalleDTO(String descripcion, int cantidad, BigDecimal precioUnitario) {}
}