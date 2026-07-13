package com.udo.can_cat.usuarios.domain.repository;

import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> findById(UsuarioId id);
    Optional<Usuario> findByCorreoElectronico(String correo);
    Usuario save(Usuario usuario);
    void delete(UsuarioId id);
}