package com.udo.can_cat.citas.infrastructure.controller;

import com.udo.can_cat.citas.application.dto.*;
import com.udo.can_cat.citas.application.service.AgendaMostradorApplicationService;
import com.udo.can_cat.citas.application.service.CitaApplicationService;
import com.udo.can_cat.citas.application.service.DisponibilidadApplicationService;
import com.udo.can_cat.usuarios.domain.entity.Personal.PersonalId;
import com.udo.can_cat.usuarios.application.dto.VeterinarioDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final DisponibilidadApplicationService disponibilidadService;
    private final CitaApplicationService citaService;
    private final AgendaMostradorApplicationService agendaMostradorService;

    public CitaController(DisponibilidadApplicationService disponibilidadService,
                          CitaApplicationService citaService,
                          AgendaMostradorApplicationService agendaMostradorService) {
        this.disponibilidadService = disponibilidadService;
        this.citaService = citaService;
        this.agendaMostradorService = agendaMostradorService;
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
     * GET /api/citas/mis-citas?estado=Pendiente_Pago
     * Retorna las citas del cliente autenticado. Si se envía 'estado',
     * filtra por ese estado (ej: Pendiente_Pago para citas por pagar).
     */
    @GetMapping("/mis-citas")
    public ResponseEntity<List<MisCitasResponseDTO>> obtenerMisCitas(
            @RequestParam(required = false) String estado) {
        List<MisCitasResponseDTO> citas = citaService.obtenerMisCitas(estado);
        return ResponseEntity.ok(citas);
    }

    /**
     * GET /api/citas/agenda
     * Parámetros (opcionalmente combinables):
     *   ?fecha=YYYY-MM-DD                      → un solo día (compatibilidad)
     *   ?fechaInicio=...&fechaFin=...          → rango
     *   (sin parámetros)                       → hoy
     */
    @GetMapping("/agenda")
    @PreAuthorize("hasRole('Recepcionista')")
    public ResponseEntity<List<CitaAgendaDTO>> consultarAgenda(
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        LocalDate inicio, fin;
        if (fecha != null) {
            inicio = fecha; fin = fecha;
        } else if (fechaInicio != null && fechaFin != null) {
            inicio = fechaInicio; fin = fechaFin;
        } else if (fechaInicio != null) {
            inicio = fechaInicio; fin = fechaInicio;
        } else {
            inicio = LocalDate.now(); fin = LocalDate.now();
        }
        return ResponseEntity.ok(agendaMostradorService.consultarAgenda(inicio, fin));
    }

    /**
     * GET /api/citas/pendientes-pago
     * Citas Pendiente_Pago (solicitadas online sin pagar) para el módulo
     * "Cobrar en Mostrador".
     */
    @GetMapping("/pendientes-pago")
    @PreAuthorize("hasRole('Recepcionista')")
    public ResponseEntity<List<CitaAgendaDTO>> listarPendientesPago() {
        return ResponseEntity.ok(agendaMostradorService.listarPendientesPago());
    }

    /**
     * POST /api/citas/{id}/cobrar-mostrador
     * Cobra presencialmente una cita Pendiente_Pago: factura + pago Confirmado
     * → cita pasa a Confirmada.
     */
    @PostMapping("/{id}/cobrar-mostrador")
    @PreAuthorize("hasRole('Recepcionista')")
    public ResponseEntity<AgendarMostradorResponseDTO> cobrarMostrador(
            @PathVariable Integer id,
            @Valid @RequestBody CobrarCitaMostradorRequestDTO request) {
        return ResponseEntity.ok(agendaMostradorService.cobrarCitaPendiente(id, request));
    }

    /**
     * POST /api/citas/{id}/cambiar-estado
     * Cambio manual de estado por recepción (matriz de transiciones en el service).
     */
    @PostMapping("/{id}/cambiar-estado")
    @PreAuthorize("hasRole('Recepcionista')")
    public ResponseEntity<CitaEstadoResponseDTO> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarEstadoCitaRequestDTO request) {
        return ResponseEntity.ok(agendaMostradorService.cambiarEstado(id, request.estado()));
    }

    /**
     * POST /api/citas/agenda-mostrador
     * CU 4.6.1.11: agenda + cobra + factura en una sola transacción.
     * 409 HorarioNoDisponible → flujo alterno 2 (choque de agendas).
     */
    @PostMapping("/agenda-mostrador")
    @PreAuthorize("hasRole('Recepcionista')")
    public ResponseEntity<AgendarMostradorResponseDTO> agendarMostrador(
            @Valid @RequestBody AgendarMostradorRequestDTO request) {
        return ResponseEntity.status(201).body(agendaMostradorService.agendarYFacturar(request));
    }
}