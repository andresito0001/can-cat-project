package com.udo.can_cat.mascotas.domain.repository;

import java.util.List;
import java.util.Optional;
import com.udo.can_cat.mascotas.domain.entity.Raza;
import com.udo.can_cat.mascotas.domain.entity.Raza.RazaId;
import com.udo.can_cat.mascotas.domain.entity.Especie.EspecieId;

public interface RazaRepository {
    List<Raza> findByEspecieId(EspecieId especieId);
    Optional<Raza> findById(RazaId id);
}