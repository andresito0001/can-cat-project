package com.udo.can_cat.almacen.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Producto {

    public record ProductoId(Integer value) {}

    private ProductoId id;
    private Integer idCategoria;
    private Integer idProveedorPredeterminado;
    private String codigoSku;
    private String nombre;
    private String descripcion;
    private String unidadMedida;
    private BigDecimal precioVenta;
    private BigDecimal costoAdquisicion;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer stockMaximo;
    private Boolean requiereReceta;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void agregarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new com.udo.can_cat.almacen.domain.exception.EntradaInvalidaException();
        }
        this.stockActual += cantidad;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public boolean requiereAlerta() {
        return this.activo && (this.stockActual <= this.stockMinimo);
    }

    public Producto() {}

    public ProductoId getId() { return id; }
    public void setId(ProductoId id) { this.id = id; }
    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
    public Integer getIdProveedorPredeterminado() { return idProveedorPredeterminado; }
    public void setIdProveedorPredeterminado(Integer idProveedorPredeterminado) { this.idProveedorPredeterminado = idProveedorPredeterminado; }
    public String getCodigoSku() { return codigoSku; }
    public void setCodigoSku(String codigoSku) { this.codigoSku = codigoSku; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    public BigDecimal getCostoAdquisicion() { return costoAdquisicion; }
    public void setCostoAdquisicion(BigDecimal costoAdquisicion) { this.costoAdquisicion = costoAdquisicion; }
    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public Integer getStockMaximo() { return stockMaximo; }
    public void setStockMaximo(Integer stockMaximo) { this.stockMaximo = stockMaximo; }
    public Boolean getRequiereReceta() { return requiereReceta; }
    public void setRequiereReceta(Boolean requiereReceta) { this.requiereReceta = requiereReceta; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}