package com.udo.can_cat.atenciones.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Puerto hacia facturación para la FACTURA DE INSUMOS (D12). La creación es
 SIN pago (la cobra recepción en Fase B). Se ejecuta dentro de la transacción
 * de guardado de la atención.
 */
public interface FacturacionPort {

    /** Correlativo de facturas del día (mismo criterio del generador FC existente). */
    long contarPorFecha(LocalDate fecha);

    FacturaCreada crearFacturaInsumos(FacturaInsumosNueva datos);

    record LineaInsumo(Integer idProducto, String descripcion, int cantidad,
                      BigDecimal precioUnitarioUsd) {}

    record FacturaInsumosNueva(Integer idCliente, Integer idPersonal, String numeroControl,
                               BigDecimal subtotal, BigDecimal porcentajeIva,
                               List<LineaInsumo> lineas) {}

    record FacturaCreada(Integer idFactura, BigDecimal totalNeto) {}
}