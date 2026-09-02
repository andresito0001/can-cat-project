package com.udo.can_cat.citas.infrastructure.controller;

import com.udo.can_cat.citas.application.dto.*;
import com.udo.can_cat.citas.application.service.CitaApplicationService;
import com.udo.can_cat.citas.application.service.DisponibilidadApplicationService;
import com.udo.can_cat.usuarios.domain.entity.Personal.PersonalId;
import com.udo.can_cat.usuarios.application.dto.VeterinarioDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final DisponibilidadApplicationService disponibilidadService;
    private final CitaApplicationService citaService;

    public CitaController(DisponibilidadApplicationService disponibilidadService,
                          CitaApplicationService citaService) {
        this.disponibilidadService = disponibilidadService;
        this.citaService = citaService;
    }

    /**
     * GET /api/citas/servicios
     * Retorna los servicios disponibles con sus precios y duraciones.
     */
    @GetMapping("/servicios")
    public ResponseEntity<List<ServicioDTO>> listarServicios(
            @RequestParam(required = false) String especialidad) {
        return ResponseEntity.ok(citaService.listarServiciosActivos(especialidad));
    }

    /**
     * GET /api/citas/veterinarios
     * Retorna los veterinarios activos para que el cliente seleccione su preferido.
     */
    @GetMapping("/veterinarios")
    public ResponseEntity<List<VeterinarioDTO>> listarVeterinarios() {
        return ResponseEntity.ok(disponibilidadService.listarVeterinarios());
    }

    /**
     * GET /api/citas/disponibilidad?id_veterinario=X&fecha=YYYY-MM-DD&id_servicio=X
     * Retorna los bloques horarios disponibles para un veterinario, fecha y servicio.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadResponseDTO> consultarDisponibilidad(
            @RequestParam Integer id_veterinario,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Integer id_servicio) {

        DisponibilidadResponseDTO respuesta = disponibilidadService.consultarDisponibilidad(
                new PersonalId(id_veterinario), fecha, id_servicio);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * POST /api/citas/solicitar
     * Crea una cita en estado PENDIENTE_PAGO (bloqueo temporal del horario).
     */
    @PostMapping("/solicitar")
    public ResponseEntity<SolicitarCitaResponseDTO> solicitarCita(
            @Valid @RequestBody SolicitarCitaRequestDTO request) {

        SolicitarCitaResponseDTO respuesta = citaService.solicitarCita(request);
        return ResponseEntity.status(201).body(respuesta);
    }

    /**
     * POST /api/citas/{id}/confirmar-pago
     * Confirma el pago de una cita (stub por ahora).
     * Cambia el estado de Pendiente_Pago → Confirmada.
     */
    @PostMapping("/{id}/confirmar-pago")
    public ResponseEntity<CitaEstadoResponseDTO> confirmarPago(@PathVariable Integer id) {
        CitaEstadoResponseDTO respuesta = citaService.confirmarPago(id);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * POST /api/citas/{id}/cancelar
     * Cancela una cita y libera el bloque horario.
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<CitaEstadoResponseDTO> cancelarCita(@PathVariable Integer id) {
        CitaEstadoResponseDTO respuesta = citaService.cancelarCita(id);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET /api/citas/mis-citas
     * Retorna todas las citas del cliente autenticado.
     */
    @GetMapping("/mis-citas")
    public ResponseEntity<List<MisCitasResponseDTO>> obtenerMisCitas() {
        List<MisCitasResponseDTO> citas = citaService.obtenerMisCitas();
        return ResponseEntity.ok(citas);
    }
}