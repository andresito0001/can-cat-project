package com.udo.can_cat.atenciones.infrastructure.controller;

import com.udo.can_cat.atenciones.application.dto.MascotaBusquedaDTO;
import com.udo.can_cat.atenciones.application.service.AtencionClinicaApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 6.2 — GET /api/mascotas/buscar. Se declara como controller NUEVO (en el módulo
 * atenciones) para NO tocar el MascotaController existente (regresión cero).
 * ⚠️ Si el MascotaController existente ya mapea "/buscar", elimina ese método
 * viejo o renombra; dos @GetMapping idénticos no pueden coexistir.
 */
@RestController
@RequestMapping("/api/mascotas")
public class MascotaBusquedaController {

    private final AtencionClinicaApplicationService service;

    public MascotaBusquedaController(AtencionClinicaApplicationService service) {
        this.service = service;
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('Veterinario','Recepcionista')")
    public ResponseEntity<List<MascotaBusquedaDTO>> buscar(
            @RequestParam(name = "filtro", required = false) String filtro) {
        return ResponseEntity.ok(service.buscarPacientes(filtro));
    }
}