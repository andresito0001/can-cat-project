package com.udo.can_cat.atenciones.application.port;

import java.util.Optional;

public interface PersonalPort {

    Optional<PersonalInfo> buscarPorUsuarioId(Integer usuarioId);

    Optional<PersonalInfo> buscarPorId(Integer personalId);

    record PersonalInfo(Integer personalId, String nombreCompleto, String especialidad,
                        String licenciaProfesional) {}
}