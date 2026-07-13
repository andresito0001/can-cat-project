package com.udo.can_cat.usuarios.infrastructure.controller;

import com.udo.can_cat.usuarios.application.dto.RolDTO;
import com.udo.can_cat.usuarios.application.service.RolApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {
    private final RolApplicationService service;

    public RolController(RolApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RolDTO> crear(@RequestBody RolDTO dto) {
        RolDTO creado = service.crearRol(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<RolDTO>> listar() {
        return ResponseEntity.ok(service.listarRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerRol(id));
    }
}