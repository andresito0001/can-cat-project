package com.udo.can_cat.usuarios.infrastructure.controller;

import java.util.List;
import com.udo.can_cat.usuarios.application.dto.ActualizarClienteRequestDTO;
import com.udo.can_cat.usuarios.application.dto.ClienteDTO;
import com.udo.can_cat.usuarios.application.dto.RegistroAsistidoRequestDTO;
import com.udo.can_cat.usuarios.application.dto.RegistroAsistidoResponseDTO;
import com.udo.can_cat.usuarios.application.service.ClienteApplicationService;
import com.udo.can_cat.usuarios.application.service.GestionClientesApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteApplicationService service;
    private final GestionClientesApplicationService gestionService;

    public ClienteController(ClienteApplicationService service,
                             GestionClientesApplicationService gestionService) {
        this.service = service;
        this.gestionService = gestionService;
    }

    // ─────────────────────────────────────────────────────────
    // Caso de uso 4.6.1.10 - Registrar Cliente (asistido por Recepcionista)
    // ─────────────────────────────────────────────────────────
    @PreAuthorize("hasRole('Recepcionista')")
    @PostMapping("/registro-asistido")
    public ResponseEntity<RegistroAsistidoResponseDTO> registrarAsistido(
            @Valid @RequestBody RegistroAsistidoRequestDTO request) {
        RegistroAsistidoResponseDTO response = gestionService.registrarClienteAsistido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────
    // Listado con filtro (solo admin + recepcionista)
    // ─────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyRole('Administrador','Recepcionista')")
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listar(
            @RequestParam(required = false) String filtro) {
        return ResponseEntity.ok(gestionService.listarClientes(filtro));
    }

    // ─────────────────────────────────────────────────────────
    // Consultas por ID / usuario / documento
    //   → Admin/Recepcionista: acceso total
    //   → Cliente: solo sobre sí mismo
    // ─────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtenerPorId(@PathVariable Integer id,
                                                   Authentication auth) {
        ClienteDTO cliente = service.obtenerPorId(id);
        if (!puedeVerCliente(cliente, auth)) {
            throw new AccessDeniedException("No tiene permiso para consultar este cliente");
        }
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ClienteDTO> obtenerPorUsuarioId(@PathVariable Integer usuarioId,
                                                          Authentication auth) {
        if (!esAdminORecepcionista(auth) && !esElMismoUsuario(usuarioId, auth)) {
            throw new AccessDeniedException("No tiene permiso para consultar este cliente");
        }
        return ResponseEntity.ok(service.obtenerPorUsuarioId(usuarioId));
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<ClienteDTO> obtenerPorDocumento(@PathVariable String documento,
                                                          Authentication auth) {
        ClienteDTO cliente = service.obtenerPorDocumento(documento);
        if (!puedeVerCliente(cliente, auth)) {
            throw new AccessDeniedException("No tiene permiso para consultar este cliente");
        }
        return ResponseEntity.ok(cliente);
    }

    // ─────────────────────────────────────────────────────────
    // Crear / actualizar
    // ─────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyRole('Administrador','Recepcionista')")
    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody ClienteDTO dto) {
        ClienteDTO creado = service.crearCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PreAuthorize("hasAnyRole('Administrador','Recepcionista')")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarClienteRequestDTO req) {
        return ResponseEntity.ok(service.actualizarCliente(id, req));
    }

    // ═════════════════════════════════════════════════════════
    // Helpers de autorización
    // ═════════════════════════════════════════════════════════

    /**
     * Verifica si el usuario autenticado puede ver los datos del cliente:
     *  - Admin o Recepcionista → sí, siempre.
     *  - Cliente dueño → sí, si el usuarioId del cliente coincide con su token.
     *  - Resto → no.
     */
    private boolean puedeVerCliente(ClienteDTO cliente, Authentication auth) {
        if (auth == null || cliente == null) return false;
        if (esAdminORecepcionista(auth)) return true;
        return esElMismoUsuario(cliente.usuarioId(), auth);
    }

    private boolean esAdminORecepcionista(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_Administrador".equals(a.getAuthority())
                            || "ROLE_Recepcionista".equals(a.getAuthority()));
    }

    private boolean esElMismoUsuario(Integer usuarioId, Authentication auth) {
        if (auth == null || usuarioId == null) return false;
        Object principal = auth.getPrincipal();
        if (principal instanceof Integer authUserId) {
            return authUserId.equals(usuarioId);
        }
        return false;
    }
}