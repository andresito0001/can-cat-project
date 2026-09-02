package com.udo.can_cat.facturacion.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import com.udo.can_cat.facturacion.domain.entity.Pago;
import com.udo.can_cat.facturacion.domain.repository.PagoRepository;
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
}