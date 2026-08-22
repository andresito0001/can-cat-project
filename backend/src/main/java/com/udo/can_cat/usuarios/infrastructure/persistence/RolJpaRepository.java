package com.udo.can_cat.usuarios.infrastructure.persistence;

import com.udo.can_cat.usuarios.infrastructure.persistence.RolJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolJpaRepository extends JpaRepository<RolJpaEntity, Integer> {
    Optional<RolJpaEntity> findByNombreRol(String nombreRol);
}