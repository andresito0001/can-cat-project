package com.udo.can_cat.almacen.infrastructure.persistence;

import com.udo.can_cat.almacen.domain.entity.DetalleCompra;
import com.udo.can_cat.almacen.domain.repository.DetalleCompraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DetalleCompraRepositoryImpl implements DetalleCompraRepository {

    private final DetalleCompraJpaRepository jpa;

    public DetalleCompraRepositoryImpl(DetalleCompraJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public DetalleCompra guardar(DetalleCompra detalle) {
        DetalleCompraJpaEntity entidad = aJpa(detalle);
        DetalleCompraJpaEntity guardado = jpa.save(entidad);
        return aDominio(guardado);
    }

    @Override
    public List<DetalleCompra> buscarPorCompraId(Integer idCompra) {
        return jpa.findAll().stream()
                .filter(e -> e.getIdCompra().equals(idCompra))
                .map(this::aDominio)
                .collect(Collectors.toList());
    }

    private DetalleCompraJpaEntity aJpa(DetalleCompra detalle) {
        DetalleCompraJpaEntity e = new DetalleCompraJpaEntity();
        e.setId(detalle.getId() != null ? detalle.getId().value() : null);
        e.setIdCompra(detalle.getIdCompra());
        e.setIdProducto(detalle.getIdProducto());
        e.setCantidadSolicitada(detalle.getCantidadSolicitada());
        e.setCantidadRecibida(detalle.getCantidadRecibida());
        e.setPrecioUnitario(detalle.getPrecioUnitario());
        e.setSubtotal(detalle.getSubtotal());
        e.setFechaVencimientoLote(detalle.getFechaVencimientoLote());
        e.setNumeroLote(detalle.getNumeroLote());
        e.setCreatedAt(detalle.getCreatedAt());
        return e;
    }

    private DetalleCompra aDominio(DetalleCompraJpaEntity e) {
        DetalleCompra detalle = new DetalleCompra();
        detalle.setId(e.getId() != null ? new DetalleCompra.DetalleId(e.getId()) : null);
        detalle.setIdCompra(e.getIdCompra());
        detalle.setIdProducto(e.getIdProducto());
        detalle.setCantidadSolicitada(e.getCantidadSolicitada());
        detalle.setCantidadRecibida(e.getCantidadRecibida());
        detalle.setPrecioUnitario(e.getPrecioUnitario());
        detalle.setSubtotal(e.getSubtotal());
        detalle.setFechaVencimientoLote(e.getFechaVencimientoLote());
        detalle.setNumeroLote(e.getNumeroLote());
        detalle.setCreatedAt(e.getCreatedAt());
        return detalle;
    }
}