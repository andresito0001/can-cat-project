package com.udo.can_cat.almacen.infrastructure.persistence;

import com.udo.can_cat.almacen.domain.entity.MovimientoInventario;
import com.udo.can_cat.almacen.domain.repository.MovimientoInventarioRepository;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public class MovimientoInventarioRepositoryImpl implements MovimientoInventarioRepository {

    private final MovimientoInventarioJpaRepository jpa;

    public MovimientoInventarioRepositoryImpl(MovimientoInventarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public MovimientoInventario guardar(MovimientoInventario movimiento) {
        MovimientoInventarioJpaEntity guardado = jpa.saveAndFlush(aJpa(movimiento));
        return aDominio(guardado);
    }

    @Override
    public List<MovimientoInventario> buscarRecientes(int limite) {
        return jpa.findTop10ByOrderByFechaMovimientoDesc(PageRequest.of(0, limite))
                .stream()
                .map(MovimientoInventarioRepositoryImpl::aDominio)
                .toList();
    }

    private static MovimientoInventarioJpaEntity aJpa(MovimientoInventario m) {
        MovimientoInventarioJpaEntity e = new MovimientoInventarioJpaEntity();
        e.setId(m.getId() != null ? m.getId().value() : null);
        e.setIdProducto(m.getIdProducto());
        e.setIdPersonal(m.getIdPersonal());
        e.setTipoMovimiento(m.getTipoMovimiento());
        e.setCantidad(m.getCantidad());
        e.setFechaMovimiento(m.getFechaMovimiento() != null ? m.getFechaMovimiento() : LocalDateTime.now());
        e.setMotivo(m.getMotivo());
        e.setDocumentoReferencia(m.getDocumentoReferencia());
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    private static MovimientoInventario aDominio(MovimientoInventarioJpaEntity e) {
        MovimientoInventario m = new MovimientoInventario();
        m.setId(e.getId() != null ? new MovimientoInventario.MovimientoId(e.getId()) : null);
        m.setIdProducto(e.getIdProducto());
        m.setIdPersonal(e.getIdPersonal());
        m.setTipoMovimiento(e.getTipoMovimiento());
        m.setCantidad(e.getCantidad());
        m.setFechaMovimiento(e.getFechaMovimiento());
        m.setMotivo(e.getMotivo());
        m.setDocumentoReferencia(e.getDocumentoReferencia());
        m.setCreatedAt(e.getCreatedAt());
        return m;
    }
}