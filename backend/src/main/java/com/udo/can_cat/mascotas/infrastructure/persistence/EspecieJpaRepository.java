package com.udo.can_cat.mascotas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecieJpaRepository extends JpaRepository<EspecieJpaEntity, Integer> {
}