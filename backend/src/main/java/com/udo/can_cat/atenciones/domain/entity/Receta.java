package com.udo.can_cat.atenciones.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Récipe digital (D5). Código «RC-yyyyMMdd-NNNNNN». Items se persisten aparte. */
public class Receta {

    public record RecetaId(Integer value) {}

    private RecetaId id;
    private Integer idAtencion;
    private String codigoReceta;
    private String indicacionesGenerales;
    private LocalDateTime fechaEmision;
    private LocalDateTime createdAt;
    private List<RecetaItem> items = new ArrayList<>();

    public Receta() {}

    public RecetaId getId() { return id; }
    public void setId(RecetaId id) { this.id = id; }
    public Integer getIdAtencion() { return idAtencion; }
    public void setIdAtencion(Integer idAtencion) { this.idAtencion = idAtencion; }
    public String getCodigoReceta() { return codigoReceta; }
    public void setCodigoReceta(String codigoReceta) { this.codigoReceta = codigoReceta; }
    public String getIndicacionesGenerales() { return indicacionesGenerales; }
    public void setIndicacionesGenerales(String indicacionesGenerales) { this.indicacionesGenerales = indicacionesGenerales; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<RecetaItem> getItems() { return items; }
    public void setItems(List<RecetaItem> items) { this.items = items; }
}