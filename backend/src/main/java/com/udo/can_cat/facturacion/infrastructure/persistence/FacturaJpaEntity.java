package com.udo.can_cat.facturacion.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.udo.can_cat.facturacion.domain.entity.Factura;

@Entity
@Table(name = "factura", uniqueConstraints = {
    @UniqueConstraint(name = "factura_numero_control_key", columnNames = "numero_control"),
    @UniqueConstraint(name = "factura_id_cita_key", columnNames = "id_cita")
})
public class FacturaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Integer idFactura;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "id_cita")
    private Integer idCita;

    @Column(name = "id_personal", nullable = false)
    private Integer idPersonal;

    @Column(name = "numero_control", length = 50, nullable = false)
    private String numeroControl;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(name = "porcentaje_iva", precision = 5, scale = 2)
    private BigDecimal porcentajeIva = new BigDecimal("16.00");

    // Campos GENERATED ALWAYS — no insertables ni actualizables por Hibernate
    @Column(name = "monto_descuento", insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal montoDescuento;

    @Column(name = "monto_iva", insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal montoIva;

    @Column(name = "total_neto", insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal totalNeto;

    @Column(name = "estado_factura", length = 20, nullable = false)
    private String estadoFactura = "Emitida";

    @Column(name = "metodo_pago_principal", length = 50)
    private String metodoPagoPrincipal;

    @Column(name = "observaciones_fiscales", columnDefinition = "text")
    private String observacionesFiscales;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.fechaEmision = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.porcentajeDescuento == null) this.porcentajeDescuento = BigDecimal.ZERO;
        if (this.porcentajeIva == null) this.porcentajeIva = new BigDecimal("16.00");
        if (this.estadoFactura == null) this.estadoFactura = "Emitida";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Factura toDomain() {
        Factura f = new Factura();
        f.setId(idFactura);
        f.setIdCliente(idCliente);
        f.setIdCita(idCita);
        f.setIdPersonal(idPersonal);
        f.setNumeroControl(numeroControl);
        f.setFechaEmision(fechaEmision);
        f.setSubtotal(subtotal);
        f.setPorcentajeDescuento(porcentajeDescuento);
        f.setPorcentajeIva(porcentajeIva);
        f.setEstadoFactura(estadoFactura);
        f.setMetodoPagoPrincipal(metodoPagoPrincipal);
        f.setObservacionesFiscales(observacionesFiscales);
        f.setCreatedAt(createdAt);
        f.setUpdatedAt(updatedAt);
        return f;
    }

    public static FacturaJpaEntity fromDomain(Factura f) {
        FacturaJpaEntity e = new FacturaJpaEntity();
        if (f.getId() != null) e.setIdFactura(f.getId());
        e.setIdCliente(f.getIdCliente());
        e.setIdCita(f.getIdCita());
        e.setIdPersonal(f.getIdPersonal());
        e.setNumeroControl(f.getNumeroControl());
        e.setFechaEmision(f.getFechaEmision());
        e.setSubtotal(f.getSubtotal());
        e.setPorcentajeDescuento(f.getPorcentajeDescuento());
        e.setPorcentajeIva(f.getPorcentajeIva());
        e.setEstadoFactura(f.getEstadoFactura());
        e.setMetodoPagoPrincipal(f.getMetodoPagoPrincipal());
        e.setObservacionesFiscales(f.getObservacionesFiscales());
        if (f.getCreatedAt() != null) e.setCreatedAt(f.getCreatedAt());
        if (f.getUpdatedAt() != null) e.setUpdatedAt(f.getUpdatedAt());
        return e;
    }

    // Getters y setters
    public Integer getIdFactura() { return idFactura; }
    public void setIdFactura(Integer idFactura) { this.idFactura = idFactura; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public Integer getIdCita() { return idCita; }
    public void setIdCita(Integer idCita) { this.idCita = idCita; }
    public Integer getIdPersonal() { return idPersonal; }
    public void setIdPersonal(Integer idPersonal) { this.idPersonal = idPersonal; }
    public String getNumeroControl() { return numeroControl; }
    public void setNumeroControl(String numeroControl) { this.numeroControl = numeroControl; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }
    public BigDecimal getPorcentajeIva() { return porcentajeIva; }
    public void setPorcentajeIva(BigDecimal porcentajeIva) { this.porcentajeIva = porcentajeIva; }
    public BigDecimal getTotalNeto() { return totalNeto; }
    public String getEstadoFactura() { return estadoFactura; }
    public void setEstadoFactura(String estadoFactura) { this.estadoFactura = estadoFactura; }
    public String getMetodoPagoPrincipal() { return metodoPagoPrincipal; }
    public void setMetodoPagoPrincipal(String metodoPagoPrincipal) { this.metodoPagoPrincipal = metodoPagoPrincipal; }
    public String getObservacionesFiscales() { return observacionesFiscales; }
    public void setObservacionesFiscales(String observacionesFiscales) { this.observacionesFiscales = observacionesFiscales; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}