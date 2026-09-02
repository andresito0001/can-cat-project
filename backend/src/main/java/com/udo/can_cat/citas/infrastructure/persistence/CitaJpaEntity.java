package com.udo.can_cat.citas.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "cita")
public class CitaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer id;

    @Column(name = "id_mascota", nullable = false)
    private Integer idMascota;

    @Column(name = "id_veterinario")
    private Integer idVeterinario;

    @Column(name = "id_servicio")
    private Integer idServicio;

    @Column(name = "id_estado", nullable = false)
    private Integer idEstado;

    @Column(name = "fecha_cita", nullable = false)
    private LocalDate fechaCita;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "motivo_consulta", nullable = false, columnDefinition = "TEXT")
    private String motivoConsulta;

    @Column(name = "tipo_atencion", nullable = false, length = 30)
    private String tipoAtencion;

    @Column(name = "costo_estimado", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "costo_usd", precision = 10, scale = 2)
    private BigDecimal costoUsd;

    @Column(name = "costo_bs", precision = 12, scale = 2)
    private BigDecimal costoBs;

    @Column(name = "tasa_cambio_aplicada", precision = 10, scale = 4)
    private BigDecimal tasaCambioAplicada;

    @Column(name = "observaciones_recepcion", columnDefinition = "TEXT")
    private String observacionesRecepcion;

    @Column(name = "fecha_solicitud", updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (fechaSolicitud == null) fechaSolicitud = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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