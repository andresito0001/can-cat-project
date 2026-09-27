package com.udo.can_cat.usuarios.application.service;

import com.udo.can_cat.usuarios.application.dto.UsuarioDTO;
import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.entity.Rol.RolId;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.entity.Usuario.EstadoUsuario;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioApplicationService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PersonalRepository personalRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioApplicationService(UsuarioRepository usuarioRepository,
                                     RolRepository rolRepository,
                                     PersonalRepository personalRepository,
                                     ClienteRepository clienteRepository,
                                     PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.personalRepository = personalRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioDTO crearUsuario(UsuarioDTO dto) {
        RolId rolId = new RolId(dto.rolId());
        rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe"));

        Usuario usuario = Usuario.crear(rolId, dto.correoElectronico(),
                passwordEncoder.encode(dto.contrasena()));

        Usuario guardado = usuarioRepository.save(usuario);
        return toDTO(guardado);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toDTO(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios(String filtro, Integer rolId, String estado) {
        List<Usuario> usuarios = usuarioRepository.findAll();

        return usuarios.stream()
                .filter(u -> rolId == null || u.getRolId().value().equals(rolId))
                .filter(u -> estado == null || estado.isBlank()
                        || u.getEstado().name().equalsIgnoreCase(estado))
                .map(this::toDTO)
                .filter(dto -> {
                    if (filtro == null || filtro.isBlank()) return true;
                    String f = filtro.toLowerCase().trim();
                    return (dto.correoElectronico() != null
                                && dto.correoElectronico().toLowerCase().contains(f))
                        || (dto.nombreCompleto() != null
                                && dto.nombreCompleto().toLowerCase().contains(f))
                        || (dto.documentoIdentidad() != null
                                && dto.documentoIdentidad().toLowerCase().contains(f));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerUsuario(Integer id) {   
        Usuario usuario = usuarioRepository.findById(new Usuario.UsuarioId(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        return toDTO(usuario);
    }

    @Transactional
    public UsuarioDTO cambiarEstado(Integer id, EstadoUsuario nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(new Usuario.UsuarioId(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setEstado(nuevoEstado);
        Usuario actualizado = usuarioRepository.save(usuario);
        return toDTO(actualizado);
    }

    // ────────────────────────────────────────────────────────
    // Mapper
    // ────────────────────────────────────────────────────────

    private UsuarioDTO toDTO(Usuario usuario) {
        String rolNombre = rolRepository.findById(usuario.getRolId())
                .map(Rol::getNombre).orElse(null);

        // Resolver identidad
        String nombreCompleto = "—";
        String documento = null;
        String tipo = "Usuario";
        Integer entidadId = null;

        var personalOpt = personalRepository.findByUsuarioId(usuario.getId());
        if (personalOpt.isPresent()) {
            var p = personalOpt.get();
            nombreCompleto = p.getNombreCompleto();
            tipo = "Personal";
            entidadId = p.getPersonalId().value();
        } else {
            var clienteOpt = clienteRepository.findByUsuarioId(usuario.getId());
            if (clienteOpt.isPresent()) {
                var c = clienteOpt.get();
                nombreCompleto = c.getNombreCompleto();
                documento = c.getDocumentoIdentidad();
                tipo = "Cliente";
                entidadId = c.getId().value();
            }
        }

        return new UsuarioDTO(
                usuario.getId().value(),
                usuario.getRolId().value(),
                usuario.getCorreoElectronico(),
                null,
                usuario.getEstado().name(),
                usuario.getFechaRegistro(),
                usuario.getUltimoAcceso(),
                rolNombre,
                nombreCompleto,
                documento,
                tipo,
                entidadId
        );
    }

    private String resolverNombreCompleto(Usuario usuario) {
        // Personal primero
        Optional<String> nombrePersonal = personalRepository
                .findByUsuarioId(usuario.getId())
                .map(p -> p.getNombreCompleto());
        if (nombrePersonal.isPresent()) return nombrePersonal.get();

        // Cliente después
        return clienteRepository.findByUsuarioId(usuario.getId())
                .map(c -> c.getNombreCompleto())
                .orElse("—");
    }
}