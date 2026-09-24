package com.udo.can_cat.atenciones.domain.exception;

public class RecetaNoEncontradaException extends RuntimeException {

    public RecetaNoEncontradaException(Integer idReceta) {
        super("Récipe no encontrado: " + idReceta);
    }
}