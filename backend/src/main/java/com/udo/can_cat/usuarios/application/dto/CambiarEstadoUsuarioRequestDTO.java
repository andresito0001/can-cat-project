package com.udo.can_cat.usuarios.application.dto;

import com.udo.can_cat.usuarios.domain.entity.Usuario.EstadoUsuario;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoUsuarioRequestDTO(@NotNull EstadoUsuario estado) {}