package com.udo.can_cat.usuarios.application.service;

import com.udo.can_cat.usuarios.application.dto.UsuarioDTO;
import com.udo.can_cat.usuarios.domain.entity.Rol.RolId;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsuarioApplicationService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UsuarioApplicationService(UsuarioRepository usuarioRepository,
                                     RolRepository rolRepository, 
                                     PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        
    }

    @Transactional
    public UsuarioDTO crearUsuario(UsuarioDTO dto) {
        RolId rolId = new RolId(dto.rolId());
        rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe"));

        Usuario usuario = Usuario.crear(rolId, dto.correoElectronico(), passwordEncoder.encode(dto.contrasena()));
        
        Usuario guardado = usuarioRepository.save(usuario);
        return toDTO(guardado);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(new Usuario.UsuarioId(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toDTO(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toDTO(usuario);
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId() != null ? usuario.getId().value() : null,
                usuario.getRolId().value(),
                usuario.getCorreoElectronico(),
                null,
                usuario.getEstado().name(),
                usuario.getFechaRegistro(),
                usuario.getUltimoAcceso()
        );
    }
}