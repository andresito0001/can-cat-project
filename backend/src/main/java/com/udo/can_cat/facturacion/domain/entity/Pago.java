package com.udo.can_cat.facturacion.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class Pago {

    private Integer id;
    private Integer idFactura;
    private Integer idMetodoPago;
    private Integer idCliente;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private String referenciaTransaccion;
    private String comprobanteAdjunto;
    private String estadoPago;
    private Integer verificadoPor;
    private LocalDateTime fechaVerificacion;
    private String observacionesVerificacion;
    private Map<String, Object> metadataJson;
    private LocalDateTime createdAt;

    public Pago() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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