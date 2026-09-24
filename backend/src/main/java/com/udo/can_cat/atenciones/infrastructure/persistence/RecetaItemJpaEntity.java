package com.udo.can_cat.atenciones.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Mapeo JPA de receta_item (V7). */
@Entity
@Table(name = "receta_item")
public class RecetaItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Integer id;

    @Column(name = "id_receta", nullable = false)
    private Integer idReceta;

    @Column(name = "medicamento", nullable = false, length = 150)
    private String medicamento;

    @Column(name = "concentracion", length = 100)
    private String concentracion;

    @Column(name = "dosis", nullable = false, length = 100)
    private String dosis;

    @Column(name = "via_administracion", length = 50)
    private String viaAdministracion;

    @Column(name = "frecuencia", nullable = false, length = 100)
    private String frecuencia;

    @Column(name = "duracion", nullable = false, length = 100)
    private String duracion;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdReceta() { return idReceta; }
    public void setIdReceta(Integer idReceta) { this.idReceta = idReceta; }
    public String getMedicamento() { return medicamento; }
    public void setMedicamento(String medicamento) { this.medicamento = medicamento; }
    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }
    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }
    public String getViaAdministracion() { return viaAdministracion; }
    public void setViaAdministracion(String viaAdministracion) { this.viaAdministracion = viaAdministracion; }
    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }
    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}