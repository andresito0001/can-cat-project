package com.udo.can_cat.usuarios.infrastructure.controller;

import com.udo.can_cat.usuarios.application.dto.UsuarioDTO;
import com.udo.can_cat.usuarios.application.service.UsuarioApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioApplicationService service;

    public UsuarioController(UsuarioApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@RequestBody UsuarioDTO dto) {
        UsuarioDTO creado = service.crearUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
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