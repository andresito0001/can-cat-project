package com.udo.can_cat.atenciones.domain.entity;

import java.time.LocalDateTime;

/**
 * Entrada del historial clínico (tabla entrada_historial, V1 — el diseño V1
 * la contempló para este CU). D11: al guardar la atención se inserta una
 * fila tipo 'Consulta' con resumen generado.
 * documentos_adjuntos (JSONB) no se escribe en este CU → columna no mapeada.
 */
public class EntradaHistorial {

    public record EntradaHistorialId(Integer value) {}

    /** Valores del CHECK tipo_registro (V1). */
    public static final String TIPO_CONSULTA = "Consulta";
    public static final String TIPO_VACUNA = "Vacuna";
    public static final String TIPO_EXAMEN = "Examen";
    public static final String TIPO_CIRUGIA = "Cirugia";

    private EntradaHistorialId id;
    private Integer idMascota;
    private Integer idAtencion;
    private String tipoRegistro;
    private LocalDateTime fechaRegistro;
    private String resumenEjecutivo;

    public EntradaHistorial() {}

    public EntradaHistorialId getId() { return id; }
    public void setId(EntradaHistorialId id) { this.id = id; }
    public Integer getIdMascota() { return idMascota; }
    public void setIdMascota(Integer idMascota) { this.idMascota = idMascota; }
    public Integer getIdAtencion() { return idAtencion; }
    public void setIdAtencion(Integer idAtencion) { this.idAtencion = idAtencion; }
    public String getTipoRegistro() { return tipoRegistro; }
    public void setTipoRegistro(String tipoRegistro) { this.tipoRegistro = tipoRegistro; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getResumenEjecutivo() { return resumenEjecutivo; }
    public void setResumenEjecutivo(String resumenEjecutivo) { this.resumenEjecutivo = resumenEjecutivo; }
}