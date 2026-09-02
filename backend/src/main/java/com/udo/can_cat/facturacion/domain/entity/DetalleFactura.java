package com.udo.can_cat.facturacion.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DetalleFactura {

    private Integer id;
    private Integer idFactura;
    private String tipoItem;
    private Integer idReferencia;
    private String descripcion;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuentoAplicado;
    private LocalDateTime createdAt;

    public DetalleFactura() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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

    public BigDecimal getSubtotal() {
        if (precioUnitario == null || cantidad == null) return BigDecimal.ZERO;
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad))
            .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}