package com.udo.can_cat.usuarios.infrastructure.controller;

import com.udo.can_cat.usuarios.application.dto.AuthResponseDTO;
import com.udo.can_cat.usuarios.application.dto.LoginRequestDTO;
import com.udo.can_cat.usuarios.application.dto.NuevaContrasenaRequestDTO;
import com.udo.can_cat.usuarios.application.dto.RecuperarPasswordRequestDTO;
import com.udo.can_cat.usuarios.application.dto.RecuperarPasswordResponseDTO;
import com.udo.can_cat.usuarios.application.dto.RegistroClienteRequestDTO;
import com.udo.can_cat.usuarios.application.dto.RegistroClienteResponseDTO;
import com.udo.can_cat.usuarios.application.service.AuthApplicationService;
import com.udo.can_cat.usuarios.application.service.RegistroApplicationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthApplicationService authApplicationService;
    private final RegistroApplicationService registroService;

    // Actualizar constructor
    public AuthController(AuthApplicationService authApplicationService,
                        RegistroApplicationService registroService) {
        this.authApplicationService = authApplicationService;
        this.registroService = registroService;
    }

    /**
     * POST /api/auth/registro
     * Registro de nuevos clientes
     */
    @PostMapping("/registro")
    public ResponseEntity<RegistroClienteResponseDTO> registrarCliente(
            @Valid @RequestBody RegistroClienteRequestDTO request) {
        logger.info("Petición de registro recibida para: {}", request.getCorreoElectronico());
        RegistroClienteResponseDTO response = registroService.registrarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * POST /api/auth/login
     * Flujo normal: Iniciar Sesión
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        logger.info("Petición de login recibida para: {}", request.getCorreoElectronico());
        AuthResponseDTO response = authApplicationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/recuperar-password
     * Flujo alternativo: Recuperación de contraseña
     */
    @PostMapping("/recuperar-password")
    public ResponseEntity<RecuperarPasswordResponseDTO> recuperarPassword(
            @Valid @RequestBody RecuperarPasswordRequestDTO request) {
        logger.info("Petición de recuperación de contraseña para: {}", request.getCorreoElectronico());
        RecuperarPasswordResponseDTO response = authApplicationService.solicitarRecuperacionPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/nueva-contrasena")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody NuevaContrasenaRequestDTO request) {
        logger.info("Petición de reset de contraseña recibida");
        authApplicationService.resetearContrasena(request);
        return ResponseEntity.ok().build();
    }
}