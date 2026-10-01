package com.udo.can_cat.usuarios.application.service;

import com.udo.can_cat.usuarios.application.dto.ActualizarPerfilRequestDTO;
import com.udo.can_cat.usuarios.application.dto.CambiarContrasenaRequestDTO;
import com.udo.can_cat.usuarios.application.dto.PerfilResponseDTO;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class PerfilApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PerfilApplicationService.class);

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PersonalRepository personalRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public PerfilApplicationService(UsuarioRepository usuarioRepository,
                                    ClienteRepository clienteRepository,
                                    PersonalRepository personalRepository,
                                    RolRepository rolRepository,
                                    PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.personalRepository = personalRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* ═══════════════════════════════════════════════════════════
       OBTENER PERFIL
       ═══════════════════════════════════════════════════════════ */
    @Transactional(readOnly = true)
    public PerfilResponseDTO obtenerPerfil(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(new UsuarioId(usuarioId))
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        String rolNombre = rolRepository.findById(usuario.getRolId())
                .map(Rol::getNombre)
                .orElse("Desconocido");

        // ─── Intentar como Personal ───
        Optional<Personal> personalOpt = personalRepository.findByUsuarioId(usuario.getId());
        if (personalOpt.isPresent()) {
            Personal p = personalOpt.get();
            return new PerfilResponseDTO(
                    usuario.getId().value(),
                    usuario.getCorreoElectronico(),
                    rolNombre,
                    p.getNombreCompleto(),
                    "Personal",
                    p.getPersonalId().value(),
                    usuario.getFechaRegistro(),
                    usuario.getUltimoAcceso(),
                    null,
                    new PerfilResponseDTO.DatosPersonal(
                            p.getCodigoEmpleado().value(),
                            p.getCargo().getDbValue(),
                            p.getEspecialidad() != null ? p.getEspecialidad().value() : null,
                            p.getLicenciaProfesional() != null ? p.getLicenciaProfesional().value() : null,
                            p.getFechaContratacion().value(),
                            p.isActivo(),
                            p.getHorarioAtencion() != null ? p.getHorarioAtencion().value() : null
                    )
            );
        }

        // ─── Fallback: Cliente ───
        Cliente c = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario autenticado no tiene perfil asociado"));

        return new PerfilResponseDTO(
                usuario.getId().value(),
                usuario.getCorreoElectronico(),
                rolNombre,
                c.getNombreCompleto(),
                "Cliente",
                c.getId().value(),
                usuario.getFechaRegistro(),
                usuario.getUltimoAcceso(),
                new PerfilResponseDTO.DatosCliente(
                        c.getDocumentoIdentidad(),
                        c.getTelefonoPrincipal(),
                        c.getTelefonoSecundario(),
                        c.getDireccion(),
                        c.getCiudad(),
                        c.getFechaNacimiento(),
                        true   // editable: solo el propio Cliente puede editar su perfil
                ),
                null
        );
    }

    /* ═══════════════════════════════════════════════════════════
       ACTUALIZAR PERFIL — SOLO CLIENTE
       ═══════════════════════════════════════════════════════════ */
    public PerfilResponseDTO actualizarPerfil(Integer usuarioId, ActualizarPerfilRequestDTO req) {
        Usuario usuario = usuarioRepository.findById(new UsuarioId(usuarioId))
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        // El personal NO edita desde aquí: sus datos pasan por administración
        Optional<Personal> personalOpt = personalRepository.findByUsuarioId(usuario.getId());
        if (personalOpt.isPresent()) {
            throw new AccessDeniedException(
                    "El personal interno no puede editar sus datos desde este endpoint. " +
                    "Contacte al administrador para actualizar su información.");
        }

        Cliente actual = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario autenticado no tiene perfil de cliente asociado"));

        Cliente actualizado = new Cliente(
                actual.getId(),
                actual.getUsuarioId(),
                req.nombreCompleto().trim(),
                actual.getDocumentoIdentidad(),     // NO se edita aquí
                req.telefonoPrincipal().trim(),
                limpiar(req.telefonoSecundario()),
                limpiar(req.direccion()),
                limpiar(req.ciudad()),
                req.fechaNacimiento(),
                actual.getPreferenciasNotificacion(),
                actual.getCreatedAt(),
                LocalDateTime.now()
        );

        clienteRepository.save(actualizado);
        log.info("Perfil actualizado para cliente id={} (usuario id={})",
                actual.getId().value(), usuarioId);

        return obtenerPerfil(usuarioId);
    }

    /* ═══════════════════════════════════════════════════════════
       CAMBIAR CONTRASEÑA (requiere contraseña actual)
       ═══════════════════════════════════════════════════════════ */
    public void cambiarContrasena(Integer usuarioId, CambiarContrasenaRequestDTO req) {
        Usuario usuario = usuarioRepository.findById(new UsuarioId(usuarioId))
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        if (!passwordEncoder.matches(req.contrasenaActual(), usuario.getContrasenaHash())) {
            throw new ContrasenaActualIncorrectaException();
        }

        if (passwordEncoder.matches(req.contrasenaNueva(), usuario.getContrasenaHash())) {
            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(req.contrasenaNueva()));
        usuarioRepository.save(usuario);

        log.info("Contraseña cambiada correctamente para usuario id={}", usuarioId);
    }

    /* ═══════════════════════════════════════════════════════════ */
    private String limpiar(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}