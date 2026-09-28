package com.udo.can_cat.almacen.domain.exception;

public class EntradaInvalidaException extends RuntimeException {
    
    public static final String MENSAJE_VALIDACION =
            "Verifique los datos ingresados. Las cantidades deben ser números mayores a cero y los campos marcados son obligatorios.";
    
    public EntradaInvalidaException() {
        super(MENSAJE_VALIDACION);
    }

    public EntradaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
