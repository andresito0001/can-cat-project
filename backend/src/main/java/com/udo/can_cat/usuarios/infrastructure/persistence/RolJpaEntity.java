package com.udo.can_cat.usuarios.infrastructure.persistence;

import com.udo.can_cat.usuarios.domain.entity.Rol;
import jakarta.persistence.*;

@Entity
@Table(name = "rol")
public class RolJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @Column(unique = true, nullable = false, length = 50)
    private String nombreRol;

    @Column(length = 255)
    private String descripcion;

    @Column(columnDefinition = "jsonb")
    private String permisosJson; 

    // Constructor vacío para JPA
    public RolJpaEntity() {}

    // Conversión desde la entidad de dominio
    public static RolJpaEntity fromDomain(Rol rol) {
        RolJpaEntity entity = new RolJpaEntity();
        if (rol.getId() != null) {
            entity.idRol = rol.getId().value();
        }
        entity.nombreRol = rol.getNombre();
        entity.descripcion = rol.getDescripcion();
        entity.permisosJson = rol.getPermisosJson();
        return entity;
    }

    // Conversión hacia la entidad de dominio
    public Rol toDomain() {
        return new Rol(
                new Rol.RolId(idRol),
                nombreRol,
                descripcion,
                permisosJson
        );
    }

    // Getters y setters solo para JPA (pueden ser package-private)
    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getPermisosJson() { return permisosJson; }
    public void setPermisosJson(String permisosJson) { this.permisosJson = permisosJson; }
}