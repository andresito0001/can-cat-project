package com.udo.can_cat.usuarios.domain.repository;

import java.util.List;
import java.util.Optional;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Cliente.ClienteId;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;

public interface ClienteRepository {
    Optional<Cliente> findById(ClienteId id);
    Optional<Cliente> findByUsuarioId(UsuarioId usuarioId);
    Optional<Cliente> findByDocumentoIdentidad(String documento);
    List<Cliente> findAll();
    List<Cliente> buscarPorFiltro(String filtro);
    Cliente save(Cliente cliente);
    void delete(ClienteId id);
}