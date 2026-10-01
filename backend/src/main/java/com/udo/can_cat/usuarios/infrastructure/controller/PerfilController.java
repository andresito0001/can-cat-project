package com.udo.can_cat.usuarios.infrastructure.controller;

import com.udo.can_cat.usuarios.application.dto.ActualizarPerfilRequestDTO;
import com.udo.can_cat.usuarios.application.dto.CambiarContrasenaRequestDTO;
import com.udo.can_cat.usuarios.application.dto.PerfilResponseDTO;
import com.udo.can_cat.usuarios.application.service.PerfilApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilApplicationService service;

    public PerfilController(PerfilApplicationService service) {
        this.service = service;
    }

    /** GET /api/perfil — Datos del usuario autenticado según su rol. */
    @GetMapping
    public ResponseEntity<PerfilResponseDTO> obtenerPerfil(Authentication auth) {
        return ResponseEntity.ok(service.obtenerPerfil(usuarioId(auth)));
    }

    /** PUT /api/perfil — Solo Clientes pueden actualizar. */
    @PutMapping
    public ResponseEntity<PerfilResponseDTO> actualizarPerfil(
            Authentication auth,
            @Valid @RequestBody ActualizarPerfilRequestDTO req) {
        return ResponseEntity.ok(service.actualizarPerfil(usuarioId(auth), req));
    }

    /** POST /api/perfil/cambiar-contrasena — Todos los roles. */
    @PostMapping("/cambiar-contrasena")
    public ResponseEntity<Void> cambiarContrasena(
            Authentication auth,
            @Valid @RequestBody CambiarContrasenaRequestDTO req) {
        service.cambiarContrasena(usuarioId(auth), req);
        return ResponseEntity.noContent().build();
    }

    /* ═══════════════════════════════════════════════════════════ */
    private Integer usuarioId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Integer id)) {
            throw new AccessDeniedException("No hay un usuario autenticado válido");
        }
        return id;
    }
}