package com.udo.can_cat.almacen.domain.exception;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String nombreProducto) {
        super("Stock insuficiente para el producto '" + nombreProducto + "'. Verifique con el almacén");
    }
}