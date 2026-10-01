package com.udo.can_cat.usuarios.application.service;

public class ContrasenaActualIncorrectaException extends RuntimeException {

    public ContrasenaActualIncorrectaException() {
        super("La contraseña actual no es correcta");
    }
}