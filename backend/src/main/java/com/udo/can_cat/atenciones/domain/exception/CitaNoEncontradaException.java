package com.udo.can_cat.atenciones.domain.exception;

public class CitaNoEncontradaException extends RuntimeException {

    public CitaNoEncontradaException(Integer idCita) {
        super("Cita no encontrada: " + idCita);
    }
}