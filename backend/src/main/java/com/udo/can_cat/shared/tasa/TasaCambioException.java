package com.udo.can_cat.shared.tasa;

public class TasaCambioException extends RuntimeException {
    public TasaCambioException(String message) {
        super(message);
    }

    public TasaCambioException(String message, Throwable cause) {
        super(message, cause);
    }
}