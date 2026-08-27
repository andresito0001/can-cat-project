package com.udo.can_cat.mascotas.domain.repository;

import java.util.List;
import java.util.Optional;
import com.udo.can_cat.mascotas.domain.entity.Mascota;
import com.udo.can_cat.mascotas.domain.entity.Mascota.MascotaId;
import com.udo.can_cat.usuarios.domain.entity.Cliente.ClienteId;

public interface MascotaRepository {
    Mascota save(Mascota mascota);
    Optional<Mascota> findById(MascotaId id);
    List<Mascota> findByClienteId(ClienteId clienteId);
}