package com.udo.can_cat.usuarios.domain.entity;

import java.util.Objects;

public class Rol {
    private RolId rol;
    private String nombre;
    private String descripcion;
    private String permisosJson; 

    public Rol(RolId id, String nombre, String descripcion, String permisosJson) {
        this.rol = id;
        this.nombre = Objects.requireNonNull(nombre, "El nombre del rol no puede ser nulo");
        this.descripcion = descripcion;
        this.permisosJson = permisosJson != null ? permisosJson : "[]";
    }

    public static Rol crear(String nombre, String descripcion, String permisosJson) {
        return new Rol(null, nombre, descripcion, permisosJson);
    }

    public void actualizarPermisos(String nuevosPermisosJson) {
        this.permisosJson = Objects.requireNonNull(nuevosPermisosJson);
    }

    // Getters
    public RolId getId() { return rol; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getPermisosJson() { return permisosJson; }

    public record RolId(Integer value) {
        public RolId {
            Objects.requireNonNull(value);
        }
    }
}