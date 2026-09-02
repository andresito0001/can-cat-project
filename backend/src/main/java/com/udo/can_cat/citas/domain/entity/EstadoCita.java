package com.udo.can_cat.citas.domain.entity;

import java.time.LocalDateTime;

public class EstadoCita {

    private Integer id;
    private String nombre;
    private String colorUi;
    private Integer ordenFlujo;
    private Boolean esFinal;
    private LocalDateTime createdAt;

    // --- Getters y Setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColorUi() { return colorUi; }
    public void setColorUi(String colorUi) { this.colorUi = colorUi; }

    public Integer getOrdenFlujo() { return ordenFlujo; }
    public void setOrdenFlujo(Integer ordenFlujo) { this.ordenFlujo = ordenFlujo; }

    public Boolean getEsFinal() { return esFinal; }
    public void setEsFinal(Boolean esFinal) { this.esFinal = esFinal; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}