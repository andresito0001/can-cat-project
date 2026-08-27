package com.udo.can_cat.mascotas.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import com.udo.can_cat.mascotas.domain.entity.Especie.EspecieId;
import com.udo.can_cat.mascotas.domain.entity.Mascota;
import com.udo.can_cat.mascotas.domain.entity.Mascota.MascotaId;
import com.udo.can_cat.mascotas.domain.entity.Raza.RazaId;
import com.udo.can_cat.mascotas.domain.repository.MascotaRepository;
import com.udo.can_cat.usuarios.domain.entity.Cliente.ClienteId;
import org.springframework.stereotype.Repository;

@Repository
public class MascotaRepositoryImpl implements MascotaRepository {

    private final MascotaJpaRepository jpaRepository;

    public MascotaRepositoryImpl(MascotaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Mascota save(Mascota mascota) {
        MascotaJpaEntity entity = toEntity(mascota);
        MascotaJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Mascota> findById(MascotaId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public List<Mascota> findByClienteId(ClienteId clienteId) {
        return jpaRepository.findByIdClienteAndActivoTrue(clienteId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    private MascotaJpaEntity toEntity(Mascota mascota) {
        MascotaJpaEntity entity = new MascotaJpaEntity();
        if (mascota.getId() != null) {
            entity.setId(mascota.getId().value());
        }
        entity.setIdCliente(mascota.getClienteId().value());
        entity.setIdEspecie(mascota.getEspecieId().value());
        if (mascota.getRazaId() != null) {
            entity.setIdRaza(mascota.getRazaId().value());
        }
        entity.setNombre(mascota.getNombre());
        entity.setFechaNacimiento(mascota.getFechaNacimiento());
        entity.setSexo(mascota.getSexo().name());
        entity.setColor(mascota.getColor());
        entity.setPesoActual(mascota.getPesoActual());
        entity.setEsterilizado(mascota.isEsterilizado());
        entity.setActivo(mascota.isActivo());
        entity.setFallecido(mascota.isFallecido());
        entity.setCreatedAt(mascota.getCreatedAt());
        entity.setUpdatedAt(mascota.getUpdatedAt());
        return entity;
    }

    private Mascota toDomain(MascotaJpaEntity entity) {
        return new Mascota(
                new MascotaId(entity.getId()),
                new ClienteId(entity.getIdCliente()),
                new EspecieId(entity.getIdEspecie()),
                entity.getIdRaza() != null ? new RazaId(entity.getIdRaza()) : null,
                entity.getNombre(),
                entity.getFechaNacimiento(),
                Mascota.Sexo.valueOf(entity.getSexo()),
                entity.getColor(),
                entity.getPesoActual(),
                Boolean.TRUE.equals(entity.getEsterilizado()),
                Boolean.TRUE.equals(entity.getActivo()),
                Boolean.TRUE.equals(entity.getFallecido()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }


}