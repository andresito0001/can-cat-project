package com.udo.can_cat.mascotas.domain.repository;

import java.util.List;
import java.util.Optional;
import com.udo.can_cat.mascotas.domain.entity.Especie;
import com.udo.can_cat.mascotas.domain.entity.Especie.EspecieId;

public interface EspecieRepository {
    List<Especie> findAll();
    Optional<Especie> findById(EspecieId id);
}