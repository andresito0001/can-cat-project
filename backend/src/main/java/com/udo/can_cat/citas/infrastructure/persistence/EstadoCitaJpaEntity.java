package com.udo.can_cat.citas.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "estado_cita")
public class EstadoCitaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "color_ui", nullable = false, length = 7)
    private String colorUi;

    @Column(name = "orden_flujo", nullable = false)
    private Integer ordenFlujo;

    @Column(name = "es_final", nullable = false)
    private Boolean esFinal;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

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