package com.udo.can_cat.citas.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MascotaNoPerteneceAlClienteException extends RuntimeException {
    public MascotaNoPerteneceAlClienteException(String message) {
        super(message);
    }
}