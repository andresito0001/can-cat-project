package com.udo.can_cat.usuarios.application.dto;

public record AuthResponseDTO (
    String token,
    String message
) {}