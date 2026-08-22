package com.udo.can_cat.usuarios.domain.repository;

import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.entity.Rol.RolId;
import java.util.List;
import java.util.Optional;

public interface RolRepository  {
    Optional<Rol> findById(RolId id);
    Optional<Rol> findByNombreRol(String nombre);
    List<Rol> findAll();
    Rol save(Rol rol);
    void delete(RolId id);
}
