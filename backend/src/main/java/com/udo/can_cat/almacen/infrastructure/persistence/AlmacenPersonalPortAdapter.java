package com.udo.can_cat.almacen.infrastructure.persistence;

import com.udo.can_cat.almacen.application.port.PersonalPort;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlmacenPersonalPortAdapter implements PersonalPort {

    private final PersonalRepository personalRepository;

    @Override
    public Optional<Integer> obtenerIdPersonalPorUsuarioId(Integer usuarioId) {
        return personalRepository.findByUsuarioId(new Usuario.UsuarioId(usuarioId))
                .map(p -> p.getPersonalId().value());
    }

    @Override
    public Optional<String> obtenerCargoPorIdPersonal(Integer idPersonal) {
        return personalRepository.findById(new Personal.PersonalId(idPersonal))
                .map(p -> p.getCargo().getDbValue());
    }
}