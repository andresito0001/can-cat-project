package com.udo.can_cat.atenciones.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AtencionClinica {

    public record AtencionId(Integer value) {}

    /** Valores del CHECK estado_atencion (V1). */
    public static final String ESTADO_EN_PROCESO = "En_Proceso";
    public static final String ESTADO_FINALIZADA = "Finalizada";
    public static final String ESTADO_DERIVADA = "Derivada";

    private AtencionId id;
    private Integer idCita;
    private Integer idVeterinario;
    private Integer idMascota;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String motivoDetallado;
    private String anamnesis;                  // V7 — obligatorio (F3)
    private String sintomasObservados;         // hallazgos físicos (F3)
    private BigDecimal pesoKg;                 // V7 — obligatorio
    private BigDecimal temperaturaC;           // V7 — obligatorio
    private Integer frecCardiaca;              // V7 — obligatorio
    private Integer frecRespiratoria;          // V7 — opcional
    private String diagnosticoPrincipal;       // obligatorio a nivel de CU
    private String diagnosticosDiferenciales;
    private String tratamientoPrescrito;
    private String observacionesGenerales;     // pronóstico / observaciones
    private String indicacionesDueno;          // V7
    private LocalDate proximaCitaRecomendada;
    private String estadoAtencion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AtencionClinica() {}

    public AtencionId getId() { return id; }
    public void setId(AtencionId id) { this.id = id; }
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