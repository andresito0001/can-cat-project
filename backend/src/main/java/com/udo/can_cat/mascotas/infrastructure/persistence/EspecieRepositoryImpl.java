package com.udo.can_cat.mascotas.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import com.udo.can_cat.mascotas.domain.entity.Especie;
import com.udo.can_cat.mascotas.domain.entity.Especie.EspecieId;
import com.udo.can_cat.mascotas.domain.repository.EspecieRepository;
import org.springframework.stereotype.Repository;

@Repository
public class EspecieRepositoryImpl implements EspecieRepository {

    private final EspecieJpaRepository jpaRepository;

    public EspecieRepositoryImpl(EspecieJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Especie> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Especie> findById(EspecieId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    private Especie toDomain(EspecieJpaEntity entity) {
        return new Especie(
                new EspecieId(entity.getId()),
                entity.getNombre(),
                entity.getDescripcion()
        );
    }
}