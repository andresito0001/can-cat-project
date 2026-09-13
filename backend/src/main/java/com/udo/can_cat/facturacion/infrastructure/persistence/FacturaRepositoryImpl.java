package com.udo.can_cat.facturacion.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import com.udo.can_cat.facturacion.domain.entity.Factura;
import com.udo.can_cat.facturacion.domain.repository.FacturaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class FacturaRepositoryImpl implements FacturaRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Factura> buscarPorId(Integer id) {
        return Optional.ofNullable(em.find(FacturaJpaEntity.class, id))
                .map(FacturaJpaEntity::toDomain);
    }

    @Override
    public Optional<Factura> buscarPorCitaId(Integer idCita) {
        try {
            FacturaJpaEntity entity = em.createQuery(
                    "SELECT f FROM FacturaJpaEntity f WHERE f.idCita = :idCita",
                    FacturaJpaEntity.class)
                    .setParameter("idCita", idCita)
                    .getSingleResult();
            return Optional.of(entity.toDomain());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Factura> buscarPorNumeroControl(String numeroControl) {
        try {
            FacturaJpaEntity entity = em.createQuery(
                    "SELECT f FROM FacturaJpaEntity f WHERE f.numeroControl = :nc",
                    FacturaJpaEntity.class)
                    .setParameter("nc", numeroControl)
                    .getSingleResult();
            return Optional.of(entity.toDomain());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Factura guardar(Factura factura) {
        FacturaJpaEntity entity = FacturaJpaEntity.fromDomain(factura);
        if (factura.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        em.flush();
        return entity.toDomain();
    }

    @Override
    public long contarPorFecha(LocalDate fecha) {
        Long count = em.createQuery(
                "SELECT COUNT(f) FROM FacturaJpaEntity f WHERE DATE(f.fechaEmision) = :fecha",
                Long.class)
                .setParameter("fecha", fecha)
                .getSingleResult();
        return count != null ? count : 0;
    }

    @Override
    public List<Factura> buscarPorClienteId(Integer idCliente) {
        return em.createQuery(
                    "SELECT f FROM FacturaJpaEntity f WHERE f.idCliente = :idCliente ORDER BY f.id DESC",
                    FacturaJpaEntity.class)
                .setParameter("idCliente", idCliente)
                .getResultList()
                .stream()
                .map(FacturaJpaEntity::toDomain)
                .toList();
    }
}