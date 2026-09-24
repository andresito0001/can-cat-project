package com.udo.can_cat.almacen.domain.entity;

import java.util.Objects;

public class CategoriaProducto {

    // 1. ID como record interno (Value Object)
    public record CategoriaId(Integer value) {
        public CategoriaId {
            if (value == null || value <= 0) {
                throw new IllegalArgumentException("El ID de la categoría debe ser un número entero positivo");
            }
        }
    }

    // 2. Campos inmutables (final)
    private final CategoriaId id;
    private final String nombre;
    private final String descripcion;
    private final Boolean requierePrescripcion;

    // 3. Constructor principal (para entidades ya persistidas o con ID conocido)
    public CategoriaProducto(CategoriaId id, String nombre, String descripcion, Boolean requierePrescripcion) {
        validarReglasDeNegocio(nombre, descripcion, requierePrescripcion);
        this.id = id; // Puede ser null si la entidad aún no se ha guardado en BD
        this.nombre = nombre.trim();
        this.descripcion = (descripcion != null) ? descripcion.trim() : null;
        this.requierePrescripcion = requierePrescripcion != null ? requierePrescripcion : false;
    }

    // 4. Constructor de fábrica (para crear nuevas categorías sin ID aún)
    public CategoriaProducto(String nombre, String descripcion, Boolean requierePrescripcion) {
        this(null, nombre, descripcion, requierePrescripcion);
    }

    // 5. Validación de invariantes del dominio
    private void validarReglasDeNegocio(String nombre, String descripcion, Boolean requierePrescripcion) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }
        if (nombre.trim().length() > 50) {
            throw new IllegalArgumentException("El nombre de la categoría no puede exceder los 50 caracteres");
        }
        if (descripcion != null && descripcion.trim().length() > 255) {
            throw new IllegalArgumentException("La descripción no puede exceder los 255 caracteres");
        }
    }

    // 6. Getters
    public CategoriaId getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Boolean getRequierePrescripcion() {
        return requierePrescripcion;
    }

    // 7. Métodos de comportamiento del dominio (Ejemplos útiles basados en tus seeds)
    public boolean esMedicamento() {
        return "Medicamento".equalsIgnoreCase(this.nombre);
    }

    public boolean esAlimento() {
        return "Alimento".equalsIgnoreCase(this.nombre);
    }

    // 8. equals y hashCode (basados en el ID si existe, o en el nombre como clave de negocio)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoriaProducto that = (CategoriaProducto) o;
        // Si tiene ID, comparamos por ID. Si no (entidad nueva), comparamos por nombre (que es UNIQUE en BD)
        if (this.id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(nombre);
    }
}