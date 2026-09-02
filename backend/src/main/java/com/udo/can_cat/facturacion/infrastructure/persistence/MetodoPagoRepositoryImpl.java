package com.udo.can_cat.facturacion.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import com.udo.can_cat.facturacion.domain.entity.MetodoPago;
import com.udo.can_cat.facturacion.domain.repository.MetodoPagoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class MetodoPagoRepositoryImpl implements MetodoPagoRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<MetodoPago> buscarPorId(Integer id) {
        return Optional.ofNullable(em.find(MetodoPagoJpaEntity.class, id))
                .map(MetodoPagoJpaEntity::toDomain);
    }

    @Override
    public List<MetodoPago> buscarActivos() {
        return em.createQuery("SELECT m FROM MetodoPagoJpaEntity m WHERE m.activo = true ORDER BY m.nombre",
                        MetodoPagoJpaEntity.class)
                .getResultList().stream().map(MetodoPagoJpaEntity::toDomain).toList();
    }

    @Override
    public MetodoPago guardar(MetodoPago metodoPago) {
        MetodoPagoJpaEntity entity = MetodoPagoJpaEntity.fromDomain(metodoPago);
        if (metodoPago.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        em.flush();
        return entity.toDomain();
    }
}