package com.udo.can_cat.atenciones.domain.entity;

/** Línea del récipe (tabla receta_item, V7). */
public class RecetaItem {

    public record RecetaItemId(Integer value) {}

    /** Vías permitidas en el wizard (F5). */
    public static final String VIA_ORAL = "Oral";
    public static final String VIA_INTRAMUSCULAR = "Intramuscular";
    public static final String VIA_SUBCUTANEA = "Subcutánea";
    public static final String VIA_INTRAVENOSA = "Intravenosa";
    public static final String VIA_TOPICA = "Tópica";
    public static final String VIA_OCULAR = "Ocular";

    private RecetaItemId id;
    private Integer idReceta;
    private String medicamento;
    private String concentracion;
    private String dosis;
    private String viaAdministracion;
    private String frecuencia;
    private String duracion;
    private Integer orden;

    public RecetaItem() {}

    public RecetaItemId getId() { return id; }
    public void setId(RecetaItemId id) { this.id = id; }
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