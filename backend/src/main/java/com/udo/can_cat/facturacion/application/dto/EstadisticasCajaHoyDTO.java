package com.udo.can_cat.facturacion.application.dto;

import java.math.BigDecimal;

/**
 * KPIs agregados para el dashboard de Caja (recepción).
 * Todos los valores son del momento actual (no históricos).
 *
 * @param totalCobradoHoyUsd  Suma de los pagos Confirmados hoy (en USD).
 * @param cantidadPagosHoy    Cantidad de pagos Confirmados hoy.
 * @param facturasPendientes  Facturas Emitida sin ningún pago registrado.
 * @param facturasUrgentes    Facturas Emitida sin pago, con más de 24h de antigüedad.
 * @param pagosPorVerificar   Pagos en estado Pendiente_Verificacion.
 */
public record EstadisticasCajaHoyDTO(
        BigDecimal totalCobradoHoyUsd,
        Integer cantidadPagosHoy,
        Integer facturasPendientes,
        Integer facturasUrgentes,
        Integer pagosPorVerificar
) {}