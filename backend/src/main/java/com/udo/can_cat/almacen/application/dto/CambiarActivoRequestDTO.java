package com.udo.can_cat.almacen.application.dto;

import jakarta.validation.constraints.NotNull;

/** Body de PATCH /api/almacen/productos/{id}/activo — no figuraba en la lista
 *  del contrato pero el endpoint lo requiere: { "activo": boolean } */
public record CambiarActivoRequestDTO(
        @NotNull Boolean activo
) {}
