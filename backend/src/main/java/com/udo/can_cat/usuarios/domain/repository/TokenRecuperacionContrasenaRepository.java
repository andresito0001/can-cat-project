package com.udo.can_cat.usuarios.domain.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.udo.can_cat.usuarios.infrastructure.persistence.TokenRecuperacionContrasenaJpaEntity;

public interface TokenRecuperacionContrasenaRepository extends JpaRepository<TokenRecuperacionContrasenaJpaEntity, String> {
    Optional<TokenRecuperacionContrasenaJpaEntity> findByToken(String token);
}
