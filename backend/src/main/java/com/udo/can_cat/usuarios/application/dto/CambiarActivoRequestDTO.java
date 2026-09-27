package com.udo.can_cat.usuarios.application.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarActivoRequestDTO(@NotNull Boolean activo) {}