package com.udo.can_cat.usuarios.infrastructure.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.udo.can_cat.usuarios.application.dto.ActualizarPersonalRequestDTO;
import com.udo.can_cat.usuarios.application.dto.CambiarActivoRequestDTO;
import com.udo.can_cat.usuarios.application.dto.PersonalDTO;
import com.udo.can_cat.usuarios.application.dto.RegistrarPersonalRequestDTO;
import com.udo.can_cat.usuarios.application.service.PersonalApplicationService;
import com.udo.can_cat.usuarios.domain.entity.Personal.Cargo;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/personal")
@PreAuthorize("hasRole('Administrador')")
public class PersonalController {

    private final PersonalApplicationService service;

    public PersonalController(PersonalApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PersonalDTO> crear(@RequestBody PersonalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearPersonal(dto));
    }

    @PostMapping("/con-usuario")
    public ResponseEntity<PersonalDTO> registrarPersonalConUsuario(
            @Valid @RequestBody RegistrarPersonalRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrarPersonalConUsuario(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonalDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarPersonalRequestDTO request) {
        return ResponseEntity.ok(service.actualizarPersonal(id, request));
    }

    @PatchMapping("/{id}/activo")
    public ResponseEntity<PersonalDTO> cambiarActivo(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarActivoRequestDTO request) {
        return ResponseEntity.ok(service.cambiarActivo(id, request.activo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPorId(@PathVariable Integer id) {
        service.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonalDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<PersonalDTO> obtenerPorIdUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorUsuarioId(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PersonalDTO> buscarPorCodigoEmpleado(@PathVariable String codigo) {
        return ResponseEntity.ok(service.buscarPorCodigoEmpleado(codigo));
    }

    @GetMapping
    public ResponseEntity<List<PersonalDTO>> listarPersonal() {
        return ResponseEntity.ok(service.listarPersonal());
    }

    @GetMapping("/cargo/{cargo}")
    public ResponseEntity<List<PersonalDTO>> listarPorCargo(@PathVariable Cargo cargo) {
        return ResponseEntity.ok(service.listarPorCargo(cargo));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<PersonalDTO>> listarActivos(@RequestParam boolean activo) {
        return ResponseEntity.ok(service.filtrarActivos(activo));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> obtenerStatsAdmin() {
        return ResponseEntity.ok(service.obtenerStatsAdmin());
    }
}