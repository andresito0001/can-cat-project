package com.udo.can_cat.usuarios.application.service;

public class TokenRecuperacionInvalidoException extends RuntimeException {

    private final String codigo;

    public TokenRecuperacionInvalidoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static TokenRecuperacionInvalidoException noExiste() {
        return new TokenRecuperacionInvalidoException(
                "TOKEN_NO_EXISTE",
                "El enlace de recuperación es inválido o ya no existe."
        );
    }

    public static TokenRecuperacionInvalidoException yaUsado() {
        return new TokenRecuperacionInvalidoException(
                "TOKEN_YA_USADO",
                "Este enlace ya fue utilizado. Solicita uno nuevo."
        );
    }

    public static TokenRecuperacionInvalidoException expirado() {
        return new TokenRecuperacionInvalidoException(
                "TOKEN_EXPIRADO",
                "El enlace de recuperación ha expirado. Solicita uno nuevo."
        );
    }

    public static TokenRecuperacionInvalidoException corrupto() {
        return new TokenRecuperacionInvalidoException(
                "TOKEN_CORRUPTO",
                "El enlace de recuperación es inválido."
        );
    }
}