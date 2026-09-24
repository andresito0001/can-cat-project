package com.udo.can_cat.atenciones.infrastructure.controller;

import com.udo.can_cat.atenciones.application.dto.AtencionGuardadaResponseDTO;
import com.udo.can_cat.atenciones.application.dto.AtencionHistorialDTO;
import com.udo.can_cat.atenciones.application.dto.CitaVetAgendaDTO;
import com.udo.can_cat.atenciones.application.dto.ContextoAtencionDTO;
import com.udo.can_cat.atenciones.application.dto.GuardarAtencionRequestDTO;
import com.udo.can_cat.atenciones.application.service.AtencionClinicaApplicationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/atenciones")
public class AtencionesController {

    private final AtencionClinicaApplicationService service;

    public AtencionesController(AtencionClinicaApplicationService service) {
        this.service = service;
    }

    /** D7: agenda PROPIA del veterinario (default hoy). */
    @GetMapping("/agenda-vet")
    @PreAuthorize("hasRole('Veterinario')")
    public ResponseEntity<List<CitaVetAgendaDTO>> agendaDelDia(
            @RequestParam(name = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(service.agendaDelVeterinario(fecha));
    }

    /** F2: contexto para el wizard (o datos en modo solo-lectura). */
    @GetMapping("/cita/{idCita}/contexto")
    @PreAuthorize("hasRole('Veterinario')")
    public ResponseEntity<ContextoAtencionDTO> contexto(@PathVariable Integer idCita) {
        return ResponseEntity.ok(service.contexto(idCita));
    }

    /** D2: Confirmada → En_Atención (idempotente). */
    @PostMapping("/cita/{idCita}/iniciar")
    @PreAuthorize("hasRole('Veterinario')")
    public ResponseEntity<Void> iniciar(@PathVariable Integer idCita) {
        service.iniciarAtencion(idCita);
        return ResponseEntity.noContent().build();
    }

    /** F7: guardado transaccional → 201 (F8). */
    @PostMapping
    @PreAuthorize("hasRole('Veterinario')")
    public ResponseEntity<AtencionGuardadaResponseDTO> guardar(
            @Valid @RequestBody GuardarAtencionRequestDTO request) {
        AtencionGuardadaResponseDTO respuesta = service.guardar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** D8: historial clínico por mascota. */
    @GetMapping("/por-mascota/{idMascota}")
    @PreAuthorize("hasAnyRole('Veterinario','Recepcionista','Cliente')")    public ResponseEntity<List<AtencionHistorialDTO>> historial(@PathVariable Integer idMascota) {
        return ResponseEntity.ok(service.historialPorMascota(idMascota));
    }
}