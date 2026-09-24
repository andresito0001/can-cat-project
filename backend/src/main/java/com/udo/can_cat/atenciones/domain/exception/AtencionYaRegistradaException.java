package com.udo.can_cat.atenciones.domain.exception;

public class AtencionYaRegistradaException extends RuntimeException {

    public AtencionYaRegistradaException() {
        super("Esta cita ya fue atendida");
    }
}