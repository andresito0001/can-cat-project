package com.udo.can_cat.facturacion.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.udo.can_cat.facturacion.domain.entity.DetalleFactura;
import com.udo.can_cat.facturacion.domain.repository.DetalleFacturaRepository;
import java.util.List;

@Repository
public class DetalleFacturaRepositoryImpl implements DetalleFacturaRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<DetalleFactura> buscarPorFacturaId(Integer idFactura) {
        return em.createQuery(
                        "SELECT d FROM DetalleFacturaJpaEntity d WHERE d.idFactura = :idFactura ORDER BY d.idDetalle",
                        DetalleFacturaJpaEntity.class)
                .setParameter("idFactura", idFactura)
                .getResultList().stream().map(DetalleFacturaJpaEntity::toDomain).toList();
    }

    @Override
    public DetalleFactura guardar(DetalleFactura detalle) {
        DetalleFacturaJpaEntity entity = DetalleFacturaJpaEntity.fromDomain(detalle);
        if (detalle.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        em.flush();
        return entity.toDomain();
    }
}