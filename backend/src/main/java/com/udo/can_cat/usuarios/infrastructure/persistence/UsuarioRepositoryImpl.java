package com.udo.can_cat.usuarios.infrastructure.persistence;

import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Usuario> findById(Usuario.UsuarioId id) {
        UsuarioJpaEntity entity = em.find(UsuarioJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(UsuarioJpaEntity::toDomain);
    }

    @Override
    public Optional<Usuario> findByCorreoElectronico(String correo) {
        String jpql = "SELECT u FROM UsuarioJpaEntity u WHERE u.correoElectronico = :correo";
        return em.createQuery(jpql, UsuarioJpaEntity.class)
                .setParameter("correo", correo)
                .getResultList()
                .stream()
                .findFirst()
                .map(UsuarioJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        UsuarioJpaEntity entity = UsuarioJpaEntity.fromDomain(usuario);
        if (usuario.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        em.flush();
        return entity.toDomain();
    }

    @Override
    @Transactional
    public void delete(Usuario.UsuarioId id) {
        UsuarioJpaEntity entity = em.find(UsuarioJpaEntity.class, id.value());
        if (entity != null) {
            em.remove(entity);
        }
    }
}