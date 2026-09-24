package com.udo.can_cat.almacen.domain.entity;

import java.time.LocalDateTime;

public class MovimientoInventario {

    public record MovimientoId(Integer value) {}

    public static final String TIPO_ENTRADA = "Entrada";
    public static final String TIPO_SALIDA = "Salida";
    public static final String TIPO_AJUSTE = "Ajuste";
    public static final String TIPO_VENCIMIENTO = "Vencimiento";

    public static final String MOTIVO_COMPRA = "Compra";
    public static final String MOTIVO_VENTA = "Venta";
    public static final String MOTIVO_CONSUMO_CLINICA = "Consumo_Clinica";
    public static final String MOTIVO_PERDIDA = "Perdida";
    public static final String MOTIVO_AJUSTE = "Ajuste";

    private MovimientoId id;
    private Integer idProducto;
    private Integer idPersonal;
    private String tipoMovimiento;
    private Integer cantidad;
    private LocalDateTime fechaMovimiento;
    private String motivo;
    private String documentoReferencia;
    private LocalDateTime createdAt;

    public MovimientoInventario() {}

    public MovimientoId getId() { return id; }
    public void setId(MovimientoId id) { this.id = id; }
    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
    public Integer getIdPersonal() { return idPersonal; }
    public void setIdPersonal(Integer idPersonal) { this.idPersonal = idPersonal; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String documentoReferencia) { this.documentoReferencia = documentoReferencia; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}