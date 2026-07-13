package com.udo.can_cat.usuarios.infrastructure.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.udo.can_cat.usuarios.application.dto.PersonalDTO;
import com.udo.can_cat.usuarios.application.service.PersonalApplicationService;
import com.udo.can_cat.usuarios.domain.entity.Personal.Cargo;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {

    private final PersonalApplicationService service;

    public PersonalController(PersonalApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PersonalDTO> crear(@RequestBody PersonalDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.crearPeronal(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPorId(@PathVariable Integer id) {
        service.eliminarPorId(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
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
}