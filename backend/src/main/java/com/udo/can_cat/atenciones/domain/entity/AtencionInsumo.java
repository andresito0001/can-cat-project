package com.udo.can_cat.atenciones.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Insumo aplicado durante la atención (tabla atencion_insumo, V7). Snapshot de precio. */
public class AtencionInsumo {

    public record AtencionInsumoId(Integer value) {}

    private AtencionInsumoId id;
    private Integer idAtencion;
    private Integer idProducto;
    private Integer cantidad;
    private BigDecimal precioUnitarioUsd;
    private LocalDateTime createdAt;

    public AtencionInsumo() {}

    public AtencionInsumoId getId() { return id; }
    public void setId(AtencionInsumoId id) { this.id = id; }
    public Integer getIdAtencion() { return idAtencion; }
    public void setIdAtencion(Integer idAtencion) { this.idAtencion = idAtencion; }
    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitarioUsd() { return precioUnitarioUsd; }
    public void setPrecioUnitarioUsd(BigDecimal precioUnitarioUsd) { this.precioUnitarioUsd = precioUnitarioUsd; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}