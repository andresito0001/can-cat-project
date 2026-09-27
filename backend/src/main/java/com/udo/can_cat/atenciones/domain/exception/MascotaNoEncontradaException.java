package com.udo.can_cat.atenciones.domain.exception;

public class MascotaNoEncontradaException extends RuntimeException {
    public MascotaNoEncontradaException(String message) {
        super(message);
    }

    public MascotaNoEncontradaException(Integer idMascota) {
        super("Mascota no encontrada con id: " + idMascota);
    }
}