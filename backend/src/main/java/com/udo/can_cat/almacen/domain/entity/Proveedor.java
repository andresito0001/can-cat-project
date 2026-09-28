package com.udo.can_cat.almacen.domain.entity;

import java.time.LocalDateTime;

public class Proveedor {
    public record ProveedorId(Integer value) {}

    private ProveedorId id;
    private String rif;
    private String nombreEmpresa;
    private String nombreContacto;
    private String telefono;
    private String correo;
    private String tipoSuministro;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String direccion;

    /** Valida que el RIF tenga formato venezolano: [JGVEP]-12345678-9 */
    public static boolean esRifValido(String rif) {
        if (rif == null) return false;
        return rif.trim().toUpperCase().matches("^[JGVEP]-\\d{8}-\\d$");
    }

    // Getters y Setters
    public ProveedorId getId() {
        return id;
    }

    public void setId(ProveedorId id) {
        this.id = id;
    }

    public String getRif() {
        return rif;
    }

    public void setRif(String rif) {
        this.rif = rif;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTipoSuministro() {
        return tipoSuministro;
    }

    public void setTipoSuministro(String tipoSuministro) {
        this.tipoSuministro = tipoSuministro;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    public String getDireccion() { 
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}