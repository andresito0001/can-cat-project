package com.udo.can_cat.citas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoCitaJpaRepository extends JpaRepository<EstadoCitaJpaEntity, Integer> {

    Optional<EstadoCitaJpaEntity> findByNombre(String nombre);

    List<EstadoCitaJpaEntity> findByEsFinalFalse();

    List<EstadoCitaJpaEntity> findByEsFinalTrue();
}