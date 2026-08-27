package com.udo.can_cat.mascotas.domain.entity;

import java.util.Objects;

public class Especie {

    private EspecieId especieId;
    private String nombre;
    private String descripcion;

    public Especie(EspecieId especieId, String nombre, String descripcion) {
        this.especieId = especieId;
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la especie es obligatorio");
        this.descripcion = descripcion;
    }

    public EspecieId getId() { return especieId; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }

    public record EspecieId(Integer value) {
        public EspecieId {
            Objects.requireNonNull(value, "El ID de especie no puede ser nulo");
        }
    }
}