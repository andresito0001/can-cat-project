package com.udo.can_cat.atenciones.domain.exception;

public class OperacionAtencionInvalidaException extends RuntimeException {
    
    public OperacionAtencionInvalidaException(String mensaje) {
        super(mensaje);
    }
}