package com.udo.can_cat.facturacion.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import com.udo.can_cat.facturacion.domain.entity.Pago;
import com.udo.can_cat.facturacion.domain.repository.PagoRepository;
import java.util.List;
import java.util.Optional;

@Repository
public class PagoRepositoryImpl implements PagoRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Pago> buscarPorId(Integer id) {
        return Optional.ofNullable(em.find(PagoJpaEntity.class, id))
                .map(PagoJpaEntity::toDomain);
    }

    @Override
    public Optional<Pago> buscarPorFacturaId(Integer idFactura) {
        try {
            PagoJpaEntity entity = em.createQuery(
                            "SELECT p FROM PagoJpaEntity p WHERE p.idFactura = :idFactura",
                            PagoJpaEntity.class)
                    .setParameter("idFactura", idFactura)
                    .getSingleResult();
            return Optional.of(entity.toDomain());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Pago guardar(Pago pago) {
        PagoJpaEntity entity = PagoJpaEntity.fromDomain(pago);
        if (pago.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        em.flush();
        return entity.toDomain();
    }
    
    @Override
    public List<Pago> buscarPorEstado(String estado) {
        return em.createQuery(
                        "SELECT p FROM PagoJpaEntity p " +
                        "WHERE p.estadoPago = :estado " +
                        "ORDER BY p.fechaPago DESC",
                        PagoJpaEntity.class)
                .setParameter("estado", estado)
                .getResultList()
                .stream()
                .map(PagoJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void actualizar(Pago pago) {
        // `guardar` ya hace merge cuando id != null → reutilizable
        guardar(pago);
    }

    @Override
    public boolean existePagoConfirmadoParaCita(Integer idCita) {
        String jpql = """
            SELECT COUNT(p) FROM PagoJpaEntity p
            WHERE p.estadoPago = 'Confirmado'
            AND p.idFactura IN (
                SELECT f.idFactura FROM FacturaJpaEntity f WHERE f.idCita = :idCita
            )
            """;
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("idCita", idCita)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public Optional<Pago> buscarPorFacturaCita(Integer idCita) {
        String jpql = """
            SELECT p FROM PagoJpaEntity p
            WHERE p.idFactura IN (
                SELECT f.idFactura FROM FacturaJpaEntity f WHERE f.idCita = :idCita
            )
            ORDER BY p.idPago DESC
            """;
        return em.createQuery(jpql, PagoJpaEntity.class)
                .setParameter("idCita", idCita)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(PagoJpaEntity::toDomain);
    }

    @Override
    public boolean existePagoActivoParaCita(Integer idCita) {
        if (idCita == null) return false;
        String jpql = """
            SELECT COUNT(p) FROM PagoJpaEntity p
            WHERE p.estadoPago IN ('Pendiente_Verificacion', 'Confirmado')
            AND p.idFactura IN (
                SELECT f.idFactura FROM FacturaJpaEntity f WHERE f.idCita = :idCita
            )
            """;
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("idCita", idCita)
                .getSingleResult();
        return count != null && count > 0;
    }
}