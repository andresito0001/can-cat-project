package com.udo.can_cat.almacen.domain.exception;

public class EntradaInvalidaException extends RuntimeException {

    /** Mensaje EXACTO requerido por el CU 4.6.1.12 para datos inválidos. */
    public static final String MENSAJE_VALIDACION =
            "Verifique los datos ingresados. Las cantidades deben ser números mayores a cero y los campos marcados son obligatorios.";

    public EntradaInvalidaException() {
        super(MENSAJE_VALIDACION);
    }
}
