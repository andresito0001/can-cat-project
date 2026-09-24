package com.udo.can_cat.atenciones.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrada_historial")
public class EntradaHistorialJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada")
    private Integer id;

    @Column(name = "id_mascota", nullable = false)
    private Integer idMascota;

    @Column(name = "id_atencion", unique = true)
    private Integer idAtencion;

    @Column(name = "tipo_registro", nullable = false, length = 20)
    private String tipoRegistro;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "resumen_ejecutivo", nullable = false, columnDefinition = "text")
    private String resumenEjecutivo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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