package com.udo.can_cat.almacen.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CompraProveedor {

    public record CompraId(Integer value) {}

    private CompraId id;
    private Integer idProveedor;
    private Integer idPersonal;
    private String numeroOrden;
    private LocalDate fechaOrden;
    private LocalDate fechaRecepcion;
    private String estadoCompra;
    private BigDecimal montoTotal;
    private String observacionesRecepcion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String numeroFacturaProveedor;

    public CompraProveedor() {}

    public CompraId getId() { return id; }
    public void setId(CompraId id) { this.id = id; }
    
    public Integer getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Integer idProveedor) { this.idProveedor = idProveedor; }
    
    public Integer getIdPersonal() { return idPersonal; }
    public void setIdPersonal(Integer idPersonal) { this.idPersonal = idPersonal; }
    
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }
    
    public LocalDate getFechaOrden() { return fechaOrden; }
    public void setFechaOrden(LocalDate fechaOrden) { this.fechaOrden = fechaOrden; }
    
    public LocalDate getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDate fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }
    
    public String getEstadoCompra() { return estadoCompra; }
    public void setEstadoCompra(String estadoCompra) { this.estadoCompra = estadoCompra; }
    
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    
    public String getObservacionesRecepcion() { return observacionesRecepcion; }
    public void setObservacionesRecepcion(String observacionesRecepcion) { this.observacionesRecepcion = observacionesRecepcion; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getNumeroFacturaProveedor() { return numeroFacturaProveedor; }
    public void setNumeroFacturaProveedor(String v) { this.numeroFacturaProveedor = v; }
}