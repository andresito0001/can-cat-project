package com.udo.can_cat.usuarios.infrastructure.persistence;

import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public class RolRepositoryImpl implements RolRepository {
    @PersistenceContext
    private EntityManager em;

    private final RolJpaRepository jpaRepository;

    public RolRepositoryImpl(RolJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<Rol> findById(Rol.RolId id) {
        RolJpaEntity entity = em.find(RolJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(RolJpaEntity::toDomain);
    }

    @Override
    public List<Rol> findAll() {
        return em.createQuery("SELECT r FROM RolJpaEntity r", RolJpaEntity.class)
                .getResultList()
                .stream()
                .map(RolJpaEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Rol save(Rol rol) {
        RolJpaEntity entity = RolJpaEntity.fromDomain(rol);
        if (rol.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        em.flush(); 
        return entity.toDomain();
    }

    @Override
    @Transactional
    public void delete(Rol.RolId id) {
        RolJpaEntity entity = em.find(RolJpaEntity.class, id.value());
        if (entity != null) {
            em.remove(entity);
        }
    }

    @Override
    public Optional<Rol> findByNombreRol(String nombre) {
        return jpaRepository.findByNombreRol(nombre)
                .map(RolJpaEntity::toDomain);
    }

}