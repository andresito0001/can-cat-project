package com.udo.can_cat.atenciones.domain.exception;

public class MascotaNoEncontradaException extends RuntimeException {

    public MascotaNoEncontradaException(Integer idMascota) {
        super("Paciente no encontrado: " + idMascota);
    }
}