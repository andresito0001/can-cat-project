package com.udo.can_cat.usuarios.infrastructure.controller;

import java.util.List;
import com.udo.can_cat.usuarios.application.dto.ClienteDTO;
import com.udo.can_cat.usuarios.application.dto.RegistroAsistidoRequestDTO;
import com.udo.can_cat.usuarios.application.dto.RegistroAsistidoResponseDTO;
import com.udo.can_cat.usuarios.application.service.ClienteApplicationService;
import com.udo.can_cat.usuarios.application.service.GestionClientesApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * Caso de uso 4.6.1.10 - Registrar Cliente (asistido por Recepcionista).
     * 409 DOCUMENTO_DUPLICADO / CORREO_DUPLICADO vía GlobalExceptionHandler.
     */
    @PreAuthorize("hasRole('Recepcionista')")
    @PostMapping("/registro-asistido")
    public ResponseEntity<RegistroAsistidoResponseDTO> registrarAsistido(
            @Valid @RequestBody RegistroAsistidoRequestDTO request) {
        RegistroAsistidoResponseDTO response = gestionService.registrarClienteAsistido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Listado y búsqueda del módulo "Gestión de Clientes".
     * GET /api/clientes            -> todos
     * GET /api/clientes?filtro=mar -> coincide por nombre o documento
     */
    @PreAuthorize("hasRole('Recepcionista')")
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listar(
            @RequestParam(required = false) String filtro) {
        return ResponseEntity.ok(gestionService.listarClientes(filtro));
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@RequestBody ClienteDTO dto) {
        ClienteDTO creado = service.crearCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ClienteDTO> obtenerPorUsuarioId(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(service.obtenerPorUsuarioId(usuarioId));
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<ClienteDTO> obtenerPorDocumento(@PathVariable String documento) {
        return ResponseEntity.ok(service.obtenerPorDocumento(documento));
    }
}