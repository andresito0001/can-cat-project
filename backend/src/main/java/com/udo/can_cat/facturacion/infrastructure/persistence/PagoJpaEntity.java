package com.udo.can_cat.facturacion.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.udo.can_cat.facturacion.domain.entity.Pago;

@Entity
@Table(name = "pago")
public class PagoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @Column(name = "id_factura", nullable = false)
    private Integer idFactura;

    @Column(name = "id_metodo_pago", nullable = false)
    private Integer idMetodoPago;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "referencia_transaccion", length = 100)
    private String referenciaTransaccion;

    @Column(name = "comprobante_adjunto", length = 500)
    private String comprobanteAdjunto;

    @Column(name = "estado_pago", length = 30, nullable = false)
    private String estadoPago = "Pendiente_Verificacion";

    @Column(name = "verificado_por")
    private Integer verificadoPor;

    @Column(name = "fecha_verificacion")
    private LocalDateTime fechaVerificacion;

    @Column(name = "observaciones_verificacion", columnDefinition = "text")
    private String observacionesVerificacion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadataJson = new HashMap<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.fechaPago = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        if (this.metadataJson == null) this.metadataJson = new HashMap<>();
        if (this.estadoPago == null) this.estadoPago = "Pendiente_Verificacion";
    }

    public Pago toDomain() {
        Pago p = new Pago();
        p.setId(idPago);
        p.setIdFactura(idFactura);
        p.setIdMetodoPago(idMetodoPago);
        p.setIdCliente(idCliente);
        p.setMonto(monto);
        p.setFechaPago(fechaPago);
        p.setReferenciaTransaccion(referenciaTransaccion);
        p.setComprobanteAdjunto(comprobanteAdjunto);
        p.setEstadoPago(estadoPago);
        p.setVerificadoPor(verificadoPor);
        p.setFechaVerificacion(fechaVerificacion);
        p.setObservacionesVerificacion(observacionesVerificacion);
        p.setMetadataJson(metadataJson);
        p.setCreatedAt(createdAt);
        return p;
    }

    public static PagoJpaEntity fromDomain(Pago p) {
        PagoJpaEntity e = new PagoJpaEntity();
        if (p.getId() != null) e.setIdPago(p.getId());
        e.setIdFactura(p.getIdFactura());
        e.setIdMetodoPago(p.getIdMetodoPago());
        e.setIdCliente(p.getIdCliente());
        e.setMonto(p.getMonto());
        e.setFechaPago(p.getFechaPago());
        e.setReferenciaTransaccion(p.getReferenciaTransaccion());
        e.setComprobanteAdjunto(p.getComprobanteAdjunto());
        e.setEstadoPago(p.getEstadoPago());
        e.setVerificadoPor(p.getVerificadoPor());
        e.setFechaVerificacion(p.getFechaVerificacion());
        e.setObservacionesVerificacion(p.getObservacionesVerificacion());
        e.setMetadataJson(p.getMetadataJson());
        return e;
    }

    // Getters y setters
    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }
    public Integer getIdFactura() { return idFactura; }
    public void setIdFactura(Integer idFactura) { this.idFactura = idFactura; }
    public Integer getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(Integer idMetodoPago) { this.idMetodoPago = idMetodoPago; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public String getReferenciaTransaccion() { return referenciaTransaccion; }
    public void setReferenciaTransaccion(String referenciaTransaccion) { this.referenciaTransaccion = referenciaTransaccion; }
    public String getComprobanteAdjunto() { return comprobanteAdjunto; }
    public void setComprobanteAdjunto(String comprobanteAdjunto) { this.comprobanteAdjunto = comprobanteAdjunto; }
    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }
    public Integer getVerificadoPor() { return verificadoPor; }
    public void setVerificadoPor(Integer verificadoPor) { this.verificadoPor = verificadoPor; }
    public LocalDateTime getFechaVerificacion() { return fechaVerificacion; }
    public void setFechaVerificacion(LocalDateTime fechaVerificacion) { this.fechaVerificacion = fechaVerificacion; }
    public String getObservacionesVerificacion() { return observacionesVerificacion; }
    public void setObservacionesVerificacion(String observacionesVerificacion) { this.observacionesVerificacion = observacionesVerificacion; }
    public Map<String, Object> getMetadataJson() { return metadataJson; }
    public void setMetadataJson(Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}