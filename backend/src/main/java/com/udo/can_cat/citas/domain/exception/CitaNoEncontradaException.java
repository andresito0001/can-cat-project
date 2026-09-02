package com.udo.can_cat.citas.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String message) {
        super(message);
    }
}