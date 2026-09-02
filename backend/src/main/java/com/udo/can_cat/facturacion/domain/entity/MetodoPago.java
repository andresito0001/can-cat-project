package com.udo.can_cat.facturacion.domain.entity;

import java.util.Map;

public class MetodoPago {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Map<String, String> datosRequeridos;
    private Boolean activo;

    public MetodoPago() {}

    public MetodoPago(Integer id, String nombre, String descripcion,
                      Map<String, String> datosRequeridos, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.datosRequeridos = datosRequeridos;
        this.activo = activo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Map<String, String> getDatosRequeridos() { return datosRequeridos; }
    public void setDatosRequeridos(Map<String, String> datosRequeridos) { this.datosRequeridos = datosRequeridos; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}