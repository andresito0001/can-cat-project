package com.udo.can_cat.mascotas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RazaJpaRepository extends JpaRepository<RazaJpaEntity, Integer> {
    List<RazaJpaEntity> findByIdEspecie(Integer idEspecie);
}