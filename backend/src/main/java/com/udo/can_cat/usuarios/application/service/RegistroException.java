package com.udo.can_cat.usuarios.application.service;

public class RegistroException extends RuntimeException {

    private final String codigo;

    public RegistroException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    // Códigos de error específicos
    public static RegistroException correoYaRegistrado() {
        return new RegistroException("CORREO_DUPLICADO", "El correo electrónico ya está registrado en el sistema");
    }

    public static RegistroException documentoYaRegistrado() {
        return new RegistroException("DOCUMENTO_DUPLICADO", "El documento de identidad ya está registrado en el sistema");
    }

    public static RegistroException rolNoEncontrado() {
        return new RegistroException("ROL_NO_ENCONTRADO", "Error de configuración: Rol de cliente no encontrado");
    }
}