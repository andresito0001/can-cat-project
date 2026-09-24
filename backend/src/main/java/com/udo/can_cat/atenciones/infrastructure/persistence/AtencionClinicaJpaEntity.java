package com.udo.can_cat.atenciones.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo JPA de atencion_clinica (V1 + columnas V7). */
@Entity
@Table(name = "atencion_clinica")
public class AtencionClinicaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atencion")
    private Integer id;

    @Column(name = "id_cita", nullable = false, unique = true)
    private Integer idCita;

    @Column(name = "id_veterinario", nullable = false)
    private Integer idVeterinario;

    @Column(name = "id_mascota", nullable = false)
    private Integer idMascota;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @Column(name = "motivo_detallado", columnDefinition = "text")
    private String motivoDetallado;

    @Column(name = "anamnesis", nullable = false, columnDefinition = "text")
    private String anamnesis;

    @Column(name = "sintomas_observados", columnDefinition = "text")
    private String sintomasObservados;

    @Column(name = "peso_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "temperatura_c", nullable = false, precision = 4, scale = 1)
    private BigDecimal temperaturaC;

    @Column(name = "frec_cardiaca", nullable = false)
    private Integer frecCardiaca;

    @Column(name = "frec_respiratoria")
    private Integer frecRespiratoria;

    @Column(name = "diagnostico_principal", columnDefinition = "text")
    private String diagnosticoPrincipal;

    @Column(name = "diagnosticos_diferenciales", columnDefinition = "text")
    private String diagnosticosDiferenciales;

    @Column(name = "tratamiento_prescrito", columnDefinition = "text")
    private String tratamientoPrescrito;

    @Column(name = "observaciones_generales", columnDefinition = "text")
    private String observacionesGenerales;

    @Column(name = "indicaciones_dueno", columnDefinition = "text")
    private String indicacionesDueno;

    @Column(name = "proxima_cita_recomendada")
    private LocalDate proximaCitaRecomendada;

    @Column(name = "estado_atencion", nullable = false, length = 20)
    private String estadoAtencion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdCita() { return idCita; }
    public void setIdCita(Integer idCita) { this.idCita = idCita; }
    public Integer getIdVeterinario() { return idVeterinario; }
    public void setIdVeterinario(Integer idVeterinario) { this.idVeterinario = idVeterinario; }
    public Integer getIdMascota() { return idMascota; }
    public void setIdMascota(Integer idMascota) { this.idMascota = idMascota; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public String getMotivoDetallado() { return motivoDetallado; }
    public void setMotivoDetallado(String motivoDetallado) { this.motivoDetallado = motivoDetallado; }
    public String getAnamnesis() { return anamnesis; }
    public void setAnamnesis(String anamnesis) { this.anamnesis = anamnesis; }
    public String getSintomasObservados() { return sintomasObservados; }
    public void setSintomasObservados(String sintomasObservados) { this.sintomasObservados = sintomasObservados; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
    public BigDecimal getTemperaturaC() { return temperaturaC; }
    public void setTemperaturaC(BigDecimal temperaturaC) { this.temperaturaC = temperaturaC; }
    public Integer getFrecCardiaca() { return frecCardiaca; }
    public void setFrecCardiaca(Integer frecCardiaca) { this.frecCardiaca = frecCardiaca; }
    public Integer getFrecRespiratoria() { return frecRespiratoria; }
    public void setFrecRespiratoria(Integer frecRespiratoria) { this.frecRespiratoria = frecRespiratoria; }
    public String getDiagnosticoPrincipal() { return diagnosticoPrincipal; }
    public void setDiagnosticoPrincipal(String diagnosticoPrincipal) { this.diagnosticoPrincipal = diagnosticoPrincipal; }
    public String getDiagnosticosDiferenciales() { return diagnosticosDiferenciales; }
    public void setDiagnosticosDiferenciales(String diagnosticosDiferenciales) { this.diagnosticosDiferenciales = diagnosticosDiferenciales; }
    public String getTratamientoPrescrito() { return tratamientoPrescrito; }
    public void setTratamientoPrescrito(String tratamientoPrescrito) { this.tratamientoPrescrito = tratamientoPrescrito; }
    public String getObservacionesGenerales() { return observacionesGenerales; }
    public void setObservacionesGenerales(String observacionesGenerales) { this.observacionesGenerales = observacionesGenerales; }
    public String getIndicacionesDueno() { return indicacionesDueno; }
    public void setIndicacionesDueno(String indicacionesDueno) { this.indicacionesDueno = indicacionesDueno; }
    public LocalDate getProximaCitaRecomendada() { return proximaCitaRecomendada; }
    public void setProximaCitaRecomendada(LocalDate proximaCitaRecomendada) { this.proximaCitaRecomendada = proximaCitaRecomendada; }
    public String getEstadoAtencion() { return estadoAtencion; }
    public void setEstadoAtencion(String estadoAtencion) { this.estadoAtencion = estadoAtencion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}