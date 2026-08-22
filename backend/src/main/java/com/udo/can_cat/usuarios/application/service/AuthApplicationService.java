package com.udo.can_cat.usuarios.application.service;

import com.udo.can_cat.usuarios.application.dto.AuthResponseDTO;
import com.udo.can_cat.usuarios.application.dto.LoginRequestDTO;
import com.udo.can_cat.usuarios.application.dto.NuevaContrasenaRequestDTO;
import com.udo.can_cat.usuarios.application.dto.RecuperarPasswordRequestDTO;
import com.udo.can_cat.usuarios.application.dto.RecuperarPasswordResponseDTO;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import com.udo.can_cat.usuarios.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthApplicationService.class);

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PersonalRepository personalRepository;
    private final RolRepository rolRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthApplicationService(UsuarioRepository usuarioRepository,
                                   ClienteRepository clienteRepository,
                                   PersonalRepository personalRepository,
                                   RolRepository rolRepository,
                                   JwtTokenProvider jwtTokenProvider,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.personalRepository = personalRepository;
        this.rolRepository = rolRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Flujo normal: Iniciar Sesión
     * Caso de uso 4.6.1.1 - Pasos 3 al 5
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        logger.info("Intento de inicio de sesión para correo: {}", request.getCorreoElectronico());

        // Paso 4: Validar información contrastándola con la BD
        Usuario usuario = usuarioRepository.findByCorreoElectronico(request.getCorreoElectronico())
                .orElseThrow(CredencialesInvalidasException::new);

        // Verificar estado del usuario (Precondición: cuenta activa)
        if (!usuario.getEstado().name().equals("Activo")) {
            logger.warn("Intento de acceso con cuenta inactiva: {} - Estado: {}", 
                request.getCorreoElectronico(), usuario.getEstado());
            throw new CuentaInactivaException(usuario.getEstado().name());
        }

        // Validar contraseña
        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasenaHash())) {
            logger.warn("Contraseña incorrecta para correo: {}", request.getCorreoElectronico());
            throw new CredencialesInvalidasException();
        }

        // Obtener rol del usuario
        Rol rol = rolRepository.findById(usuario.getRolId())
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado para el usuario"));

        // Obtener nombre completo según tipo de usuario
        String nombreCompleto = obtenerNombreCompleto(usuario.getId());

        // Actualizar último acceso
        usuarioRepository.updateUltimoAcceso(usuario.getId());

        // Paso 5: Generar token JWT y construir respuesta
        String token = jwtTokenProvider.generateToken(usuario, rol.getNombre());
        
        AuthResponseDTO.UsuarioAuthDTO usuarioAuth = AuthResponseDTO.UsuarioAuthDTO.of(
                usuario.getId().value(),
                usuario.getCorreoElectronico(),
                rol.getNombre(),
                nombreCompleto
        );

        logger.info("Inicio de sesión exitoso para: {} - Rol: {}", 
            request.getCorreoElectronico(), rol.getNombre());

        return AuthResponseDTO.of(token, jwtTokenProvider.getExpirationInSeconds(), usuarioAuth);
    }

    /**
     * Flujo alternativo: Recuperación de contraseña
     * Caso de uso 4.6.1.1 - Flujo alternativo 1
     */
    @Transactional(readOnly = true)
    public RecuperarPasswordResponseDTO solicitarRecuperacionPassword(RecuperarPasswordRequestDTO request) {
        logger.info("Solicitud de recuperación de contraseña para: {}", request.getCorreoElectronico());

        // Verificar si el correo existe en el sistema
        boolean correoExiste = usuarioRepository.existsByCorreoElectronico(request.getCorreoElectronico());
        
        if (!correoExiste) {
            // Por seguridad, no indicamos si el correo existe o no
            logger.info("Correo no registrado: {}", request.getCorreoElectronico());
            return RecuperarPasswordResponseDTO.correoNoRegistrado();
        }

        // Obtener el usuario
        Usuario usuario = usuarioRepository.findByCorreoElectronico(request.getCorreoElectronico())
                .orElseThrow();

        // Verificar si es CLIENTE o PERSONAL
        boolean esCliente = clienteRepository.findByUsuarioId(usuario.getId()).isPresent();

        if (esCliente) {
            // Si es Cliente: enviar enlace de recuperación
            String tokenRecuperacion = jwtTokenProvider.generatePasswordResetToken(usuario);
            
            emailService.enviarCorreoRecuperacionPassword(
                    usuario.getCorreoElectronico(), 
                    tokenRecuperacion
            );
            
            logger.info("Enlace de recuperación enviado a: {}", usuario.getCorreoElectronico());
            return RecuperarPasswordResponseDTO.enlaceEnviado(
                    ocultarCorreo(usuario.getCorreoElectronico())
            );
        } else {
            // Si es personal: bloquear por política
            logger.warn("Intento de recuperación por personal interno: {}", 
                usuario.getCorreoElectronico());
            return RecuperarPasswordResponseDTO.bloqueadoPorPolitica();
        }
    }

    @Transactional
    public void resetearContrasena(NuevaContrasenaRequestDTO request) {
        logger.info("Procesando reset de contraseña");

        // 1. Validar que el token sea válido y no haya expirado
        if (!jwtTokenProvider.validateToken(request.getToken()) && !jwtTokenProvider.isPasswordResetToken(request.getToken())) {
            throw new RuntimeException("El enlace de recuperación es inválido o ha expirado");
        }

        // 2. Extraer el correo/username del token
        String correo = jwtTokenProvider.getEmailFromToken(request.getToken());
        logger.info("Token válido para el correo: {}", correo);

        // 3. Buscar el usuario
        Usuario usuario = usuarioRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 4. Actualizar la contraseña (necesitas este método en tu entidad Usuario)
        usuario.setContrasenaHash(passwordEncoder.encode(request.getNuevaContrasena()));
        usuario = usuarioRepository.save(usuario);
        
        logger.info("Contraseña actualizada correctamente para: {}", correo);
    }

    /**
     * Obtiene el nombre completo del usuario dependiendo de si es Cliente o Personal
     */
    private String obtenerNombreCompleto(UsuarioId usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId)
                .map(Cliente::getNombreCompleto)
                .orElse("Desconocido");
    }

    /**
     * Oculta parte del correo por seguridad en la respuesta
     * Ejemplo: juan@gmail.com -> j***@gmail.com
     */
    private String ocultarCorreo(String correo) {
        int atIndex = correo.indexOf("@");
        if (atIndex <= 1) {
            return "***" + correo.substring(atIndex);
        }
        return correo.charAt(0) + "***" + correo.substring(atIndex);
    }
}