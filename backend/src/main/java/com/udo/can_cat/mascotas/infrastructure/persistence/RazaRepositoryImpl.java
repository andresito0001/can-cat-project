package com.udo.can_cat.mascotas.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import com.udo.can_cat.mascotas.domain.entity.Especie.EspecieId;
import com.udo.can_cat.mascotas.domain.entity.Raza;
import com.udo.can_cat.mascotas.domain.entity.Raza.RazaId;
import com.udo.can_cat.mascotas.domain.repository.RazaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class RazaRepositoryImpl implements RazaRepository {

    private final RazaJpaRepository jpaRepository;

    public RazaRepositoryImpl(RazaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Raza> findByEspecieId(EspecieId especieId) {
        return jpaRepository.findByIdEspecie(especieId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Raza> findById(RazaId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    private Raza toDomain(RazaJpaEntity entity) {
        return new Raza(
                new RazaId(entity.getId()),
                new EspecieId(entity.getIdEspecie()),
                entity.getNombre(),
                entity.getCaracteristicas()
        );
    }
}