package com.udo.can_cat.citas.infrastructure.persistence;

import com.udo.can_cat.citas.domain.entity.EstadoCita;
import com.udo.can_cat.citas.domain.repository.EstadoCitaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class EstadoCitaRepositoryImpl implements EstadoCitaRepository {

    private final EstadoCitaJpaRepository jpaRepository;

    public EstadoCitaRepositoryImpl(EstadoCitaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<EstadoCita> buscarPorNombre(String nombre) {
        return jpaRepository.findByNombre(nombre).map(this::toDomain);
    }

    @Override
    public List<EstadoCita> buscarTodos() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Integer> buscarIdsEstadosActivos() {
        return jpaRepository.findByEsFinalFalse().stream()
                .map(EstadoCitaJpaEntity::getId).toList();
    }

    @Override
    public List<Integer> buscarIdsEstadosFinales() {
        return jpaRepository.findByEsFinalTrue().stream()
                .map(EstadoCitaJpaEntity::getId).toList();
    }

    private EstadoCita toDomain(EstadoCitaJpaEntity e) {
        EstadoCita d = new EstadoCita();
        d.setId(e.getId());
        d.setNombre(e.getNombre());
        d.setColorUi(e.getColorUi());
        d.setOrdenFlujo(e.getOrdenFlujo());
        d.setEsFinal(e.getEsFinal());
        d.setCreatedAt(e.getCreatedAt());
        return d;
    }
}