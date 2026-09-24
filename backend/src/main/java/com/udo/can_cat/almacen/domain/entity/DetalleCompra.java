package com.udo.can_cat.almacen.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DetalleCompra {

    public record DetalleId(Integer value) {}

    private DetalleId id;
    private Integer idCompra;
    private Integer idProducto;
    private Integer cantidadSolicitada;
    private Integer cantidadRecibida;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal; // Calculado por la BD, se usa solo para lectura en dominio
    private LocalDate fechaVencimientoLote;
    private String numeroLote;
    private LocalDateTime createdAt;

    public DetalleCompra() {}

    public DetalleId getId() { return id; }
    public void setId(DetalleId id) { this.id = id; }
    
    public Integer getIdCompra() { return idCompra; }
    public void setIdCompra(Integer idCompra) { this.idCompra = idCompra; }
    
    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
    
    public Integer getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(Integer cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    
    public Integer getCantidadRecibida() { return cantidadRecibida; }
    public void setCantidadRecibida(Integer cantidadRecibida) { this.cantidadRecibida = cantidadRecibida; }
    
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public LocalDate getFechaVencimientoLote() { return fechaVencimientoLote; }
    public void setFechaVencimientoLote(LocalDate fechaVencimientoLote) { this.fechaVencimientoLote = fechaVencimientoLote; }
    
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}