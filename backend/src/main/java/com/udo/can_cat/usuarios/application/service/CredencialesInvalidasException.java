package com.udo.can_cat.usuarios.application.service;

public class CredencialesInvalidasException extends RuntimeException {
    
    public CredencialesInvalidasException() {
        super("Credenciales incorrectas");
    }

    public CredencialesInvalidasException(String message) {
        super(message);
    }
}