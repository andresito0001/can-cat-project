package com.udo.can_cat.shared.validation;

public final class DocumentoIdentidad {

    private DocumentoIdentidad() {}

    public static final String PATTERN = "^[VEJPGvejpg]-\\d{6,9}$";

    public static final String MENSAJE =
            "Formato de documento inválido. Ejemplos válidos: V-12345678, E-123456, J-123456789";

    /**
     * Normaliza al formato canónico V-12345678.
     */
    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String raw = valor.trim().toUpperCase();
        if (raw.matches("^[VEJPG]-\\d+$")) return raw;
        if (raw.matches("^[VEJPG]\\d+$")) return raw.charAt(0) + "-" + raw.substring(1);
        if (raw.matches("^\\d+$")) return "V-" + raw;
        return raw;
    }
}