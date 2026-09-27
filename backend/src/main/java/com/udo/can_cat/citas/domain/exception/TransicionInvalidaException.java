package com.udo.can_cat.citas.domain.exception;

public class TransicionInvalidaException extends RuntimeException {
    public TransicionInvalidaException(String message) {
        super(message);
    }
}