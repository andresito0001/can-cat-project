package com.udo.can_cat.citas.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Cita {

    private Integer id;
    private Integer idMascota;
    private Integer idVeterinario;
    private Integer idServicio;
    private Integer idEstado;
    private LocalDate fechaCita;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String motivoConsulta;
    private String tipoAtencion;
    private BigDecimal costoEstimado;
    private BigDecimal costoUsd;
    private BigDecimal costoBs;
    private BigDecimal tasaCambioAplicada;
    private String observacionesRecepcion;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- Getters y Setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdMascota() { return idMascota; }
    public void setIdMascota(Integer idMascota) { this.idMascota = idMascota; }

    public Integer getIdVeterinario() { return idVeterinario; }
    public void setIdVeterinario(Integer idVeterinario) { this.idVeterinario = idVeterinario; }

    public Integer getIdServicio() { return idServicio; }
    public void setIdServicio(Integer idServicio) { this.idServicio = idServicio; }

    public Integer getIdEstado() { return idEstado; }
    public void setIdEstado(Integer idEstado) { this.idEstado = idEstado; }

    public LocalDate getFechaCita() { return fechaCita; }
    public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }

    public String getTipoAtencion() { return tipoAtencion; }
    public void setTipoAtencion(String tipoAtencion) { this.tipoAtencion = tipoAtencion; }

    public BigDecimal getCostoEstimado() { return costoEstimado; }
    public void setCostoEstimado(BigDecimal costoEstimado) { this.costoEstimado = costoEstimado; }

    public BigDecimal getCostoUsd() { return costoUsd; }
    public void setCostoUsd(BigDecimal costoUsd) { this.costoUsd = costoUsd; }

    public BigDecimal getCostoBs() { return costoBs; }
    public void setCostoBs(BigDecimal costoBs) { this.costoBs = costoBs; }

    public BigDecimal getTasaCambioAplicada() { return tasaCambioAplicada; }
    public void setTasaCambioAplicada(BigDecimal tasaCambioAplicada) { this.tasaCambioAplicada = tasaCambioAplicada; }

    public String getObservacionesRecepcion() { return observacionesRecepcion; }
    public void setObservacionesRecepcion(String observacionesRecepcion) { this.observacionesRecepcion = observacionesRecepcion; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}