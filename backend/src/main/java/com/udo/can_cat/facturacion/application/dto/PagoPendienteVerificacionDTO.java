package com.udo.can_cat.facturacion.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record PagoPendienteVerificacionDTO(
        Integer idPago,
        Integer idFactura,
        String numeroControl,
        BigDecimal monto,
        BigDecimal montoBs,
        String metodoPago,
        String referenciaTransaccion,
        Map<String, Object> metadataPago,
        LocalDateTime fechaPago,
        String clienteNombre,
        String clienteDocumento,
        String clienteTelefono,
        Integer idCita,
        String mascotaNombre
) {}