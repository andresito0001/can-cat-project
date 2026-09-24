package com.udo.can_cat.facturacion.infrastructure.persistence;

import com.udo.can_cat.facturacion.application.dto.FacturaPendienteDTO;
import com.udo.can_cat.facturacion.application.port.CobroMostradorPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CobroMostradorAdapter implements CobroMostradorPort {

    private final EntityManager em;

    public CobroMostradorAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<FacturaPendienteDTO> listarFacturasPendientes() {
        List<Object[]> filas = em.createNativeQuery("""
                        SELECT f.id_factura, f.numero_control, f.fecha_emision, f.total_neto,
                               cl.id_cliente, cl.nombre_completo, cl.documento_identidad
                        FROM factura f
                        JOIN cliente cl ON cl.id_cliente = f.id_cliente
                        WHERE f.estado_factura = 'Emitida'
                          AND NOT EXISTS (SELECT 1 FROM pago p WHERE p.id_factura = f.id_factura)
                        ORDER BY f.fecha_emision DESC
                        """)
                .getResultList();
        if (filas.isEmpty()) return List.of();

        List<Integer> ids = filas.stream().map(f -> aEntero(f[0])).toList();
        Map<Integer, List<FacturaPendienteDTO.DetalleDTO>> detallesPorFactura =
                cargarDetalles(ids);

        return filas.stream().map(f -> new FacturaPendienteDTO(
                aEntero(f[0]),                                    // idFactura
                (String) f[1],                                    // numeroControl
                aFechaHora(f[2]),                                 // fechaEmision
                new FacturaPendienteDTO.ClienteDTO((String) f[5], (String) f[6]),  // cliente
                detallesPorFactura.getOrDefault(aEntero(f[0]), List.of()),          // detalles
                aDecimal(f[3])))                                  // totalNeto
                .toList();
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, List<FacturaPendienteDTO.DetalleDTO>> cargarDetalles(List<Integer> idsFactura) {
        List<Object[]> filas = em.createNativeQuery("""
                        SELECT id_factura, descripcion, cantidad, precio_unitario
                        FROM detalle_factura
                        WHERE id_factura IN (:ids)
                        ORDER BY id_detalle ASC
                        """)
                .setParameter("ids", idsFactura)
                .getResultList();
        Map<Integer, List<FacturaPendienteDTO.DetalleDTO>> mapa = new HashMap<>();
        for (Object[] f : filas) {
            mapa.computeIfAbsent(aEntero(f[0]), k -> new ArrayList<>())
                .add(new FacturaPendienteDTO.DetalleDTO(
                        (String) f[1],
                        aEntero(f[2]) != null ? aEntero(f[2]) : 0,
                        aDecimal(f[3])));
        }
        return mapa;
    }

    // ── conversores locales (SqlConverters de atenciones es package-private) ──
    private static Integer aEntero(Object v) {
        if (v == null) return null;
        return (v instanceof Number n) ? n.intValue() : Integer.valueOf(v.toString());
    }

    private static BigDecimal aDecimal(Object v) {
        if (v == null) return null;
        return (v instanceof BigDecimal b) ? b : new BigDecimal(v.toString());
    }

    private static LocalDateTime aFechaHora(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime dt) return dt;
        if (v instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return LocalDateTime.parse(v.toString());
    }
}