package com.udo.can_cat.usuarios.infrastructure.persistence;

import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Cliente.ClienteId;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public class ClienteRepositoryImpl implements ClienteRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Cliente> findById(ClienteId id) {
        ClienteJpaEntity entity = em.find(ClienteJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(ClienteJpaEntity::toDomain);
    }

    @Override
    public Optional<Cliente> findByUsuarioId(UsuarioId usuarioId) {
        String jpql = "SELECT c FROM ClienteJpaEntity c WHERE c.idUsuario = :usuarioId";
        return em.createQuery(jpql, ClienteJpaEntity.class)
                .setParameter("usuarioId", usuarioId.value())
                .getResultList()
                .stream()
                .findFirst()
                .map(ClienteJpaEntity::toDomain);
    }

    @Override
    public Optional<Cliente> findByDocumentoIdentidad(String documento) {
        String jpql = "SELECT c FROM ClienteJpaEntity c WHERE c.documentoIdentidad = :doc";
        return em.createQuery(jpql, ClienteJpaEntity.class)
                .setParameter("doc", documento)
                .getResultList()
                .stream()
                .findFirst()
                .map(ClienteJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        ClienteJpaEntity entity = ClienteJpaEntity.fromDomain(cliente);
        if (cliente.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        em.flush();
        return entity.toDomain();
    }

    @Override
    @Transactional
    public void delete(ClienteId id) {
        ClienteJpaEntity entity = em.find(ClienteJpaEntity.class, id.value());
        if (entity != null) {
            em.remove(entity);
        }
    }
}