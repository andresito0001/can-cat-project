package com.udo.can_cat.facturacion.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Factura {

    private Integer id;
    private Integer idCliente;
    private Integer idCita;
    private Integer idPersonal;
    private String numeroControl;
    private LocalDateTime fechaEmision;
    private BigDecimal subtotal;
    private BigDecimal porcentajeDescuento;
    private BigDecimal porcentajeIva;
    private String estadoFactura;
    private String metodoPagoPrincipal;
    private String observacionesFiscales;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Factura() {}

    // Getters y setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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

    public BigDecimal getTotalNeto() {
        if (subtotal == null) return BigDecimal.ZERO;
        BigDecimal base = subtotal.subtract(
            subtotal.multiply(porcentajeDescuento != null ? porcentajeDescuento : BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP));
        BigDecimal iva = base.multiply(porcentajeIva != null ? porcentajeIva : BigDecimal.ZERO)
            .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
        return base.add(iva).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}