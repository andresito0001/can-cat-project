package com.udo.can_cat.almacen.domain.exception;

public class ProveedorNoEncontradoException extends RuntimeException {
    public ProveedorNoEncontradoException(Integer idProveedor) {
        super("El proveedor con id " + idProveedor + " no existe o está inactivo.");
    }
}
