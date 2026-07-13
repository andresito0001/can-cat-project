package com.udo.can_cat.usuarios.infrastructure.controller;

import com.udo.can_cat.usuarios.application.dto.ClienteDTO;
import com.udo.can_cat.usuarios.application.service.ClienteApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteApplicationService service;

    public ClienteController(ClienteApplicationService service) {
        this.service = service;
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