package com.udo.can_cat.usuarios.application.service;

public class CuentaInactivaException extends RuntimeException {
    
    private final String estado;

    public CuentaInactivaException(String estado) {
        super("La cuenta se encuentra " + estado);
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }
}