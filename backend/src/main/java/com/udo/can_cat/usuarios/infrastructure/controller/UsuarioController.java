package com.udo.can_cat.usuarios.infrastructure.controller;

import com.udo.can_cat.usuarios.application.dto.CambiarEstadoUsuarioRequestDTO;
import com.udo.can_cat.usuarios.application.dto.UsuarioDTO;
import com.udo.can_cat.usuarios.application.service.UsuarioApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('Administrador')")
public class UsuarioController {

    private final UsuarioApplicationService service;

    public UsuarioController(UsuarioApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Integer rolId,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(service.listarUsuarios(filtro, rolId, estado));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioDTO> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarEstadoUsuarioRequestDTO request) {
        return ResponseEntity.ok(service.cambiarEstado(id, request.estado()));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@RequestBody UsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearUsuario(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerUsuario(id));
    }

    @GetMapping("/por-correo")
    public ResponseEntity<UsuarioDTO> obtenerPorCorreo(@RequestParam String correo) {
        return ResponseEntity.ok(service.obtenerPorCorreo(correo));
    }
}