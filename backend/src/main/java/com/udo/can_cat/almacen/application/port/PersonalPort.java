package com.udo.can_cat.almacen.application.port;

import java.util.Optional;

public interface PersonalPort {

    /** id_personal del personal asociado al usuarioId (principal del JWT). */
    Optional<Integer> obtenerIdPersonalPorUsuarioId(Integer usuarioId);

    /** Cargo del personal ('Encargado_Almacen', 'Veterinario', ...) o vacío. */
    Optional<String> obtenerCargoPorIdPersonal(Integer idPersonal);
}
