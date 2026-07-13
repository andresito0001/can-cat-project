package com.udo.can_cat.usuarios.application.service;

import com.udo.can_cat.usuarios.application.dto.RolDTO;
import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RolApplicationService {
    private final RolRepository rolRepository;

    public RolApplicationService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Transactional
    public RolDTO crearRol(RolDTO dto) {
        Rol rol = Rol.crear(dto.nombre(), dto.descripcion(), dto.permisosJs());
        Rol guardado = rolRepository.save(rol);
        return toDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<RolDTO> listarRoles() {
        return rolRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RolDTO obtenerRol(Integer id) {
        Rol rol = rolRepository.findById(new Rol.RolId(id))
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        return toDTO(rol);
    }

    private RolDTO toDTO(Rol rol) {
        return new RolDTO(
                rol.getId() != null ? rol.getId().value() : null,
                rol.getNombre(),
                rol.getDescripcion(),
                rol.getPermisosJson()
        );
    }
}