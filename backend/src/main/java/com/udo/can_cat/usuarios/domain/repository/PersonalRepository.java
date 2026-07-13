package com.udo.can_cat.usuarios.domain.repository;

import java.util.List;
import java.util.Optional;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Personal.Cargo;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;

public interface PersonalRepository {
    Personal save(Personal personal);
    Personal deleteById(Personal.PersonalId personalId);
    Optional<Personal> findById(Personal.PersonalId personalId);
    Optional<Personal> findByUsuarioId(UsuarioId usuarioId);
    Optional<Personal> findByCodigoEmpleado(Personal.CodigoEmpleado codigoEmpleado);
    List<Personal> findAll();
    List<Personal> findAllByCargo(Cargo cargo);
    List<Personal> findAllByActivo(boolean activo);
    boolean existsById(Personal.PersonalId personalId);
    boolean existsByUsuarioId(UsuarioId usuarioId);
    boolean existsByCodigoEmpleado(Personal.CodigoEmpleado codigoEmpleado);
}