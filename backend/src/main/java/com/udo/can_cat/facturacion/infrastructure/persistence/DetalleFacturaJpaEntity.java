package com.udo.can_cat.facturacion.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.udo.can_cat.facturacion.domain.entity.DetalleFactura;

@Entity
@Table(name = "detalle_factura")
public class DetalleFacturaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @Column(name = "id_factura", nullable = false)
    private Integer idFactura;

    @Column(name = "tipo_item", length = 30, nullable = false)
    private String tipoItem;

    @Column(name = "id_referencia", nullable = false)
    private Integer idReferencia;

    @Column(name = "descripcion", length = 255, nullable = false)
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "descuento_aplicado", precision = 10, scale = 2)
    private BigDecimal descuentoAplicado = BigDecimal.ZERO;

    // GENERATED ALWAYS
    @Column(name = "subtotal", insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.cantidad == null) this.cantidad = 1;
        if (this.descuentoAplicado == null) this.descuentoAplicado = BigDecimal.ZERO;
    }

    public DetalleFactura toDomain() {
        DetalleFactura d = new DetalleFactura();
        d.setId(idDetalle);
        d.setIdFactura(idFactura);
        d.setTipoItem(tipoItem);
        d.setIdReferencia(idReferencia);
        d.setDescripcion(descripcion);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(precioUnitario);
        d.setDescuentoAplicado(descuentoAplicado);
        d.setCreatedAt(createdAt);
        return d;
    }

    public static DetalleFacturaJpaEntity fromDomain(DetalleFactura d) {
        DetalleFacturaJpaEntity e = new DetalleFacturaJpaEntity();
        if (d.getId() != null) e.setIdDetalle(d.getId());
        e.setIdFactura(d.getIdFactura());
        e.setTipoItem(d.getTipoItem());
        e.setIdReferencia(d.getIdReferencia());
        e.setDescripcion(d.getDescripcion());
        e.setCantidad(d.getCantidad());
        e.setPrecioUnitario(d.getPrecioUnitario());
        e.setDescuentoAplicado(d.getDescuentoAplicado());
        return e;
    }

    // Getters y setters
    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }
    public Integer getIdFactura() { return idFactura; }
    public void setIdFactura(Integer idFactura) { this.idFactura = idFactura; }
    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }
    public Integer getIdReferencia() { return idReferencia; }
    public void setIdReferencia(Integer idReferencia) { this.idReferencia = idReferencia; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(BigDecimal descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}