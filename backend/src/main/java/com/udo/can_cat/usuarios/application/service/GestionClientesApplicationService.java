package com.udo.can_cat.usuarios.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.udo.can_cat.usuarios.application.dto.ClienteDTO;
import com.udo.can_cat.usuarios.application.dto.RegistroAsistidoRequestDTO;
import com.udo.can_cat.usuarios.application.dto.RegistroAsistidoResponseDTO;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import com.udo.can_cat.usuarios.domain.repository.TokenRecuperacionContrasenaRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import com.udo.can_cat.usuarios.infrastructure.persistence.TokenRecuperacionContrasenaJpaEntity;
import com.udo.can_cat.usuarios.infrastructure.security.JwtTokenProvider;

/**
 * Caso de uso 4.6.1.10 - Registrar Cliente (asistido por Recepcionista)
 * Incluye listado y búsqueda para el módulo "Gestión de Clientes".
 */
@Service
@Transactional
public class GestionClientesApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(GestionClientesApplicationService.class);
    private static final String ROL_CLIENTE = "Cliente";
    private static final int HORAS_VALIDEZ_INVITACION = 24;

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final TokenRecuperacionContrasenaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public GestionClientesApplicationService(UsuarioRepository usuarioRepository,
                                              ClienteRepository clienteRepository,
                                              RolRepository rolRepository,
                                              TokenRecuperacionContrasenaRepository tokenRepository,
                                              PasswordEncoder passwordEncoder,
                                              JwtTokenProvider jwtTokenProvider,
                                              EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.rolRepository = rolRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }

    /**
     * Flujo normal 4.6.1.10: registrar cliente en mostrador.
     * Flujo alterno 2: documento duplicado -> RegistroException (409 DOCUMENTO_DUPLICADO).
     * El cliente queda Activo, pero sin contraseña conocida: la define con el
     * enlace de invitación (reutiliza la infraestructura de reset de contraseña).
     */
    public RegistroAsistidoResponseDTO registrarClienteAsistido(RegistroAsistidoRequestDTO request) {
        String documento = request.getDocumentoIdentidad().trim();
        String correo = request.getCorreoElectronico().toLowerCase().trim();

        logger.info("Registro asistido de cliente. Documento: {}, Recepcionista registra en mostrador", documento);

        // Paso 4: validar duplicados (correo por login, documento por perfil)
        if (usuarioRepository.existsByCorreoElectronico(correo)) {
            logger.warn("Registro asistido con correo duplicado: {}", correo);
            throw RegistroException.correoYaRegistrado();
        }
        if (clienteRepository.findByDocumentoIdentidad(documento).isPresent()) {
            logger.warn("Registro asistido con documento duplicado: {}", documento);
            throw RegistroException.documentoYaRegistrado();
        }

        // Rol Cliente
        Rol rolCliente = rolRepository.findByNombreRol(ROL_CLIENTE)
                .orElseThrow(RegistroException::rolNoEncontrado);

        // Paso 5: crear Usuario con contraseña aleatoria DESCONOCIDA.
        // Nadie puede iniciar sesión con ella; el cliente define la suya con el enlace.
        Usuario usuario = Usuario.crear(rolCliente.getId(), correo,
                passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario = usuarioRepository.save(usuario);
        logger.info("Usuario creado (asistido) con ID: {}", usuario.getId());

        // Crear perfil Cliente
        Cliente cliente = Cliente.crear(
                usuario.getId(),
                request.getNombreCompleto().trim(),
                documento,
                request.getTelefonoPrincipal().trim(),
                limpiarCampo(request.getTelefonoSecundario()),
                limpiarCampo(request.getDireccion()),
                limpiarCampo(request.getCiudad()),
                parsearFecha(request.getFechaNacimiento()),
                null
        );
        cliente = clienteRepository.save(cliente);
        logger.info("Cliente creado (asistido) con ID: {}", cliente.getId());

        // Paso 6: invitación por correo. Si falla, NO se revierte el registro (alterno tolerante):
        // el cliente puede usar "¿Olvidaste tu contraseña?" del login.
        boolean invitacionEnviada = enviarInvitacionDefinirContrasena(usuario, cliente.getNombreCompleto());

        return RegistroAsistidoResponseDTO.exito(
                cliente.getId().value(),
                usuario.getId().value(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                usuario.getCorreoElectronico(),
                invitacionEnviada
        );
    }

    /**
     * Genera el token de definición de contraseña y envía el correo de invitación.
     * Reutiliza el mecanismo de recuperación de contraseña existente.
     */
    private boolean enviarInvitacionDefinirContrasena(Usuario usuario, String nombreCliente) {
        try {
            String token = jwtTokenProvider.generatePasswordResetToken(usuario);

            TokenRecuperacionContrasenaJpaEntity invitacion = new TokenRecuperacionContrasenaJpaEntity();
            invitacion.setToken(token);
            invitacion.setCorreo(usuario.getCorreoElectronico());
            invitacion.setFechaExpiracion(LocalDateTime.now().plusHours(HORAS_VALIDEZ_INVITACION));
            tokenRepository.save(invitacion);

            emailService.enviarCorreoInvitacionCliente(usuario.getCorreoElectronico(), nombreCliente, token);
            return true;
        } catch (Exception e) {
            logger.error("No se pudo enviar la invitación a {}: {}", usuario.getCorreoElectronico(), e.getMessage());
            return false;
        }
    }

    /**
     * Listado/búsqueda para el módulo "Gestión de Clientes".
     * @param filtro texto parcial sobre nombre o documento (opcional)
     */
    @Transactional(readOnly = true)
    public List<ClienteDTO> listarClientes(String filtro) {
        List<Cliente> clientes;
        if (filtro == null || filtro.isBlank()) {
            clientes = clienteRepository.findAll();
        } else {
            clientes = clienteRepository.buscarPorFiltro(filtro.trim().toLowerCase());
        }
        return clientes.stream().map(this::toDTO).toList();
    }

    private ClienteDTO toDTO(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId() != null ? cliente.getId().value() : null,
                cliente.getUsuarioId().value(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getTelefonoPrincipal(),
                cliente.getTelefonoSecundario(),
                cliente.getDireccion(),
                cliente.getCiudad(),
                cliente.getFechaNacimiento(),
                cliente.getPreferenciasNotificacion(),
                cliente.getCreatedAt(),
                cliente.getUpdatedAt()
        );
    }

    private LocalDate parsearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            logger.warn("Fecha de nacimiento inválida: {}", fechaStr);
            return null;
        }
    }

    private String limpiarCampo(String campo) {
        if (campo == null || campo.isBlank()) {
            return null;
        }
        return campo.trim();
    }
}