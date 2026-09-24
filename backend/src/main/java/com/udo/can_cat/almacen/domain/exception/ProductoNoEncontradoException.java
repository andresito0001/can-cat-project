package com.udo.can_cat.almacen.domain.exception;

// NOTA: si StockInsuficienteException usa otra convención (p.ej. @ResponseStatus
// o constructores distintos), se alinea este estilo en la versión final.
public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(Integer idProducto) {
        super("El producto con id " + idProducto + " no existe o está inactivo.");
    }
}
