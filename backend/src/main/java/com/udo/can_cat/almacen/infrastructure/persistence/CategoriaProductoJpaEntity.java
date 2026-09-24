package com.udo.can_cat.almacen.infrastructure.persistence;

import com.udo.can_cat.almacen.domain.entity.CategoriaProducto;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_producto")
public class CategoriaProductoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria; // <-- Cambiado de Long a Integer para coincidir con el record


    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "requiere_prescripcion")
    private Boolean requierePrescripcion;

    // --- Constructores ---
    public CategoriaProductoJpaEntity() {
    }

    public CategoriaProductoJpaEntity(Integer idCategoria, String nombre, String descripcion, Boolean requierePrescripcion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.requierePrescripcion = requierePrescripcion;
    }

    // --- Getters y Setters ---
    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getRequierePrescripcion() {
        return requierePrescripcion;
    }

    public void setRequierePrescripcion(Boolean requierePrescripcion) {
        this.requierePrescripcion = requierePrescripcion;
    }

    public CategoriaProducto toDomain() {
        // ⚠️ IMPORTANTE: Ajusta los parámetros de este constructor 
        // para que coincidan exactamente con cómo está definido tu 
        // objeto de dominio CategoriaProducto y su CategoriaId.
        
        // Ejemplo si tu dominio usa un Value Object para el ID:
        return new CategoriaProducto(new CategoriaProducto.CategoriaId(this.idCategoria), this.nombre, this.descripcion, this.requierePrescripcion);
        
        // Ejemplo si tu dominio usa Long directamente:
        // return new CategoriaProducto(this.idCategoria, this.nombre, this.descripcion, this.requierePrescripcion);
    }
}