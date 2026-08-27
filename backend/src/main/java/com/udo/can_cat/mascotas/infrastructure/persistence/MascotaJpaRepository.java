package com.udo.can_cat.mascotas.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MascotaJpaRepository extends JpaRepository<MascotaJpaEntity, Integer> {
    List<MascotaJpaEntity> findByIdClienteAndActivoTrue(Integer idCliente);
}