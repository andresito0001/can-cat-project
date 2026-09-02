package com.udo.can_cat.citas.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class HorarioNoDisponibleException extends RuntimeException {
    public HorarioNoDisponibleException(String message) {
        super(message);
    }
}