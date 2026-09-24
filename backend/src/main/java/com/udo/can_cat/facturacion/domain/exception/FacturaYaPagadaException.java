package com.udo.can_cat.facturacion.domain.exception;

public class FacturaYaPagadaException extends RuntimeException {

    public FacturaYaPagadaException(String numeroControl) {
        super("La factura " + numeroControl + " ya tiene un pago registrado");
    }
}