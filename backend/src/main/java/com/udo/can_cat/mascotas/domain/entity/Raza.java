package com.udo.can_cat.mascotas.domain.entity;

import java.util.Objects;

public class Raza {

    private RazaId razaId;
    private Especie.EspecieId especieId;
    private String nombre;
    private String caracteristicas;

    public Raza(RazaId razaId, Especie.EspecieId especieId, String nombre, String caracteristicas) {
        this.razaId = razaId;
        this.especieId = Objects.requireNonNull(especieId, "El ID de especie es obligatorio");
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la raza es obligatorio");
        this.caracteristicas = caracteristicas;
    }

    public RazaId getId() { return razaId; }
    public Especie.EspecieId getEspecieId() { return especieId; }
    public String getNombre() { return nombre; }
    public String getCaracteristicas() { return caracteristicas; }

    public record RazaId(Integer value) {
        public RazaId {
            Objects.requireNonNull(value, "El ID de raza no puede ser nulo");
        }
    }
}