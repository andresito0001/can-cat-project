package com.udo.can_cat.usuarios.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Personal.Cargo;
import com.udo.can_cat.usuarios.domain.entity.Personal.CodigoEmpleado;
import com.udo.can_cat.usuarios.domain.entity.Personal.PersonalId;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class PersonalRepositoryImpl implements PersonalRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Personal save(Personal personal) {
        PersonalJpaEntity entry = PersonalJpaEntity.fromDomain(personal);

        if(personal.getPersonalId() == null) {
            em.persist(entry);
        } else  {
            entry = em.merge(entry);
        }

        em.flush();

        return entry.toDomain();
    }

    @Override
    public Personal deleteById(PersonalId personalId) {
        return Optional.ofNullable(em.find(PersonalJpaEntity.class, personalId.value()))
            .map(entry -> {
                final Personal domain = entry.toDomain();
                em.remove(entry);
                return domain;
            })
            .orElseThrow(() -> new EntityNotFoundException("No se pudo eliminar: ID no encontrado"));
    }

    @Override
    public Optional<Personal> findById(PersonalId id) {
        PersonalJpaEntity entity = em.find(PersonalJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(PersonalJpaEntity::toDomain);
    }

    @Override
    public Optional<Personal> findByUsuarioId(UsuarioId id) {
        String jpql = "SELECT p FROM PersonalJpaEntity p WHERE p.usuario.idUsuario = :usuarioId";
        
        try {
            PersonalJpaEntity entity = em.createQuery(jpql, PersonalJpaEntity.class)
                    .setParameter("usuarioId", id.value())
                    .getSingleResult();
            
            return Optional.of(entity.toDomain());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Personal> findByCodigoEmpleado(CodigoEmpleado codigo) {
        String jpql = "SELECT p FROM PersonalJpaEntity p WHERE p.codigoEmpleado = :codigo";
        
        try {
            PersonalJpaEntity entity = em.createQuery(jpql, PersonalJpaEntity.class)
                    .setParameter("codigo", codigo.value())
                    .getSingleResult();
            
            return Optional.of(entity.toDomain());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Personal> findAll() {
        String jpql = "SELECT p FROM PersonalJpaEntity p";
        
        return em.createQuery(jpql, PersonalJpaEntity.class)
                .getResultList()
                .stream()
                .map(PersonalJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Personal> findAllByCargo(Cargo cargo) {
        String jpql = "SELECT p FROM PersonalJpaEntity p WHERE p.cargo = :cargo";
        
        return em.createQuery(jpql, PersonalJpaEntity.class)
                .setParameter("cargo", cargo.valueOf(cargo.name()))
                .getResultList()
                .stream()
                .map(PersonalJpaEntity::toDomain)
                .toList();
    }


    @Override
    public List<Personal> findAllByActivo(boolean activo) {
        String jpql = "SELECT p FROM PersonalJpaEntity p WHERE p.activo = :activo";
        
        return em.createQuery(jpql, PersonalJpaEntity.class)
                .setParameter("activo", activo)
                .getResultList()
                .stream()
                .map(PersonalJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(PersonalId personalId) {
        String jpql = "SELECT COUNT(p) FROM PersonalJpaEntity p WHERE p.idPersonal = :id";
        
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("id", personalId.value())
                .getSingleResult();
        
        return count > 0;
    }

    @Override
    public boolean existsByUsuarioId(UsuarioId usuarioId) {
        String jpql = "SELECT COUNT(p) FROM PersonalJpaEntity p WHERE p.usuario.idUsuario = :usuarioId";
        
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("usuarioId", usuarioId.value())
                .getSingleResult();
        
        return count > 0;
    }

    @Override
    public boolean existsByCodigoEmpleado(CodigoEmpleado codigoEmpleado) {
        String jpql = "SELECT COUNT(p) FROM PersonalJpaEntity p WHERE p.codigoEmpleado = :codigo";
        
        Long count = em.createQuery(jpql, Long.class)
            .setParameter("codigo", codigoEmpleado.value())
            .getSingleResult();
        
        return count > 0;
    }

    @Override
    public List<Personal> findAllByCargoAndActivo(Cargo cargo, boolean activo) {
        String jpql = "SELECT p FROM PersonalJpaEntity p WHERE p.cargo = :cargo AND p.activo = :activo";
        
        return em.createQuery(jpql, PersonalJpaEntity.class)
                .setParameter("cargo", cargo.getDbValue())
                .setParameter("activo", activo)
                .getResultList()
                .stream()
                .map(PersonalJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Personal> findAllByIds(Set<PersonalId> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        Set<Integer> rawIds = ids.stream()
                .map(Personal.PersonalId::value)
                .collect(Collectors.toSet());
        String jpql = "SELECT p FROM PersonalJpaEntity p WHERE p.idPersonal IN :ids";
        return em.createQuery(jpql, PersonalJpaEntity.class)
                .setParameter("ids", rawIds)
                .getResultList()
                .stream()
                .map(PersonalJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Personal> findByIdAndCargoAndActivo(
            Personal.PersonalId id, 
            Personal.Cargo cargo, 
            boolean activo) {

        String jpql = "SELECT p FROM PersonalJpaEntity p " +
                    "WHERE p.idPersonal = :idPersonal AND p.cargo = :cargo AND p.activo = :activo";

        try {
            PersonalJpaEntity entity = em.createQuery(jpql, PersonalJpaEntity.class)
                    .setParameter("idPersonal", id.value())
                    .setParameter("cargo", cargo.getDbValue())
                    .setParameter("activo", activo)
                    .getSingleResult();
            return Optional.of(entity.toDomain());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}