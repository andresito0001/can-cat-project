package com.udo.can_cat.atenciones.infrastructure.persistence;

import com.udo.can_cat.atenciones.application.port.FacturacionPort;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class FacturacionPortAdapter implements FacturacionPort {

    private static final Logger log = LoggerFactory.getLogger(FacturacionPortAdapter.class);

    private final EntityManager em;

    public FacturacionPortAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    public long contarPorFecha(LocalDate fecha) {
        Object total = em.createNativeQuery("""
                        SELECT COUNT(*) FROM factura
                        WHERE fecha_emision >= :inicio AND fecha_emision < :fin
                        """)
                .setParameter("inicio", fecha.atStartOfDay())
                .setParameter("fin", fecha.plusDays(1).atStartOfDay())
                .getSingleResult();
        return ((Number) total).longValue();
    }

    @Override
    public FacturaCreada crearFacturaInsumos(FacturaInsumosNueva datos) {
        int filas = em.createNativeQuery("""
                        INSERT INTO factura (id_cliente, id_cita, id_personal, numero_control,
                                             subtotal, porcentaje_descuento, porcentaje_iva,
                                             estado_factura, metodo_pago_principal, fecha_emision)
                        VALUES (:idCliente, NULL, :idPersonal, :numeroControl,
                                :subtotal, 0, :porcentajeIva,
                                'Emitida', NULL, CURRENT_TIMESTAMP)
                        """)
                .setParameter("idCliente", datos.idCliente())
                .setParameter("idPersonal", datos.idPersonal())
                .setParameter("numeroControl", datos.numeroControl())
                .setParameter("subtotal", datos.subtotal())
                .setParameter("porcentajeIva", datos.porcentajeIva())
                .executeUpdate();
        if (filas != 1) {
            throw new IllegalStateException("No se pudo crear la factura de productos");
        }

        // total_neto lo calcula la BD (GENERATED) → se relee para el retorno:
        Object[] fila = (Object[]) em.createNativeQuery("""
                        SELECT id_factura, total_neto FROM factura
                        WHERE numero_control = :numeroControl
                        """)
                .setParameter("numeroControl", datos.numeroControl())
                .getSingleResult();
        Integer idFactura = SqlConverters.aEntero(fila[0]);
        BigDecimal totalNeto = SqlConverters.aDecimal(fila[1]);

        for (LineaInsumo linea : datos.lineas()) {
            em.createNativeQuery("""
                            INSERT INTO detalle_factura (id_factura, tipo_item, id_referencia,
                                                         descripcion, cantidad, precio_unitario, descuento_aplicado)
                            VALUES (:idFactura, 'Producto_Farmacia', :idReferencia,
                                    :descripcion, :cantidad, :precioUnitario, 0)
                            """)
                    .setParameter("idFactura", idFactura)
                    .setParameter("idReferencia", linea.idProducto())
                    .setParameter("descripcion", linea.descripcion())
                    .setParameter("cantidad", linea.cantidad())
                    .setParameter("precioUnitario", linea.precioUnitarioUsd())
                    .executeUpdate();
        }

        log.info("Factura de productos creada: idFactura={}, numeroControl={}, total(BD)={}, lineas={}",
                idFactura, datos.numeroControl(), totalNeto, datos.lineas().size());
        return new FacturaCreada(idFactura, totalNeto);
    }
}