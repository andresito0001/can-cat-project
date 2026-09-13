package com.udo.can_cat.usuarios.application.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.udo.can_cat.usuarios.application.dto.RegistroClienteRequestDTO;
import com.udo.can_cat.usuarios.application.dto.RegistroClienteResponseDTO;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.entity.Rol.RolId;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;

@Service
@Transactional
public class RegistroApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(RegistroApplicationService.class);
    private static final String ROL_CLIENTE = "Cliente";

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroApplicationService(UsuarioRepository usuarioRepository,
                                       ClienteRepository clienteRepository,
                                       RolRepository rolRepository,
                                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registrar un nuevo cliente en el sistema
     */
    public RegistroClienteResponseDTO registrarCliente(RegistroClienteRequestDTO request) {
        logger.info("Iniciando registro de cliente con correo: {}", request.getCorreoElectronico());

        // 1. Validar que el correo no exista
        if (usuarioRepository.existsByCorreoElectronico(request.getCorreoElectronico())) {
            logger.warn("Intento de registro con correo duplicado: {}", request.getCorreoElectronico());
            throw RegistroException.correoYaRegistrado();
        }

        // 2. Obtener el rol de Cliente
        Rol rolCliente = rolRepository.findByNombreRol(ROL_CLIENTE)
                .orElseThrow(RegistroException::rolNoEncontrado);

        // 3. Crear entidad Usuario
        Usuario usuario = crearUsuario(request, rolCliente.getId());

        usuario = usuarioRepository.save(usuario);
        logger.info("Usuario creado con ID: {}", usuario.getId());

        // 4. Crear entidad Cliente
        Cliente cliente = crearCliente(request, usuario.getId());
        cliente = clienteRepository.save(cliente);
        logger.info("Cliente creado con ID: {}", cliente.getId());

        return RegistroClienteResponseDTO.exito(
                usuario.getId().value(),
                cliente.getId().value(),
                usuario.getCorreoElectronico(),
                cliente.getNombreCompleto()
        );
    }

    /**
     * Crear entidad Usuario a partir del DTO
     */
    private Usuario crearUsuario(RegistroClienteRequestDTO request, RolId rolId) {
        return Usuario.crear(rolId, request.getCorreoElectronico().toLowerCase().trim(),
        passwordEncoder.encode(request.getContrasena()));
    }

    /**
     * Crear entidad Cliente a partir del DTO
     */
    private Cliente crearCliente(RegistroClienteRequestDTO request, UsuarioId usuarioId) {
        return Cliente.crear (
            usuarioId, request.getNombreCompleto().trim(),
            request.getDocumentoIdentidad().trim(),
            request.getTelefonoPrincipal().trim(),
            limpiarCampo(request.getTelefonoSecundario()),
            limpiarCampo(request.getDireccion()),
            limpiarCampo(request.getCiudad()),
            parsearFecha(request.getFechaNacimiento()),
            null
        );

    }

    /**
     * Parsear fecha de nacimiento
     */
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

    /**
     * Limpiar campo opcional (retornar null si está vacío)
     */
    private String limpiarCampo(String campo) {
        if (campo == null || campo.isBlank()) {
            return null;
        }
        return campo.trim();
    }
}