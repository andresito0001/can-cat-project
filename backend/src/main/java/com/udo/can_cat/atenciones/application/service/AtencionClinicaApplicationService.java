package com.udo.can_cat.atenciones.application.service;

import com.udo.can_cat.almacen.domain.entity.MovimientoInventario;
import com.udo.can_cat.almacen.domain.entity.Producto;
import com.udo.can_cat.almacen.domain.exception.StockInsuficienteException;
import com.udo.can_cat.almacen.domain.repository.MovimientoInventarioRepository;
import com.udo.can_cat.almacen.domain.repository.ProductoRepository;
import com.udo.can_cat.atenciones.application.dto.AtencionGuardadaResponseDTO;
import com.udo.can_cat.atenciones.application.dto.AtencionHistorialDTO;
import com.udo.can_cat.atenciones.application.dto.CitaVetAgendaDTO;
import com.udo.can_cat.atenciones.application.dto.ContextoAtencionDTO;
import com.udo.can_cat.atenciones.application.dto.GuardarAtencionRequestDTO;
import com.udo.can_cat.atenciones.application.dto.MascotaBusquedaDTO;
import com.udo.can_cat.atenciones.application.port.CitaPort;
import com.udo.can_cat.atenciones.application.port.FacturacionPort;
import com.udo.can_cat.atenciones.application.port.MascotaPort;
import com.udo.can_cat.atenciones.application.port.MascotaPort.MascotaInfo;
import com.udo.can_cat.atenciones.application.port.PersonalPort;
import com.udo.can_cat.atenciones.domain.entity.AtencionClinica;
import com.udo.can_cat.atenciones.domain.entity.AtencionInsumo;
import com.udo.can_cat.atenciones.domain.entity.EntradaHistorial;
import com.udo.can_cat.atenciones.domain.entity.Receta;
import com.udo.can_cat.atenciones.domain.entity.RecetaItem;
import com.udo.can_cat.atenciones.domain.exception.AtencionYaRegistradaException;
import com.udo.can_cat.atenciones.domain.exception.CitaNoEncontradaException;
import com.udo.can_cat.atenciones.domain.exception.MascotaNoEncontradaException;
import com.udo.can_cat.atenciones.domain.exception.OperacionAtencionInvalidaException;
import com.udo.can_cat.atenciones.domain.repository.AtencionClinicaRepository;
import com.udo.can_cat.atenciones.domain.repository.AtencionInsumoRepository;
import com.udo.can_cat.atenciones.domain.repository.EntradaHistorialRepository;
import com.udo.can_cat.atenciones.domain.repository.RecetaItemRepository;
import com.udo.can_cat.atenciones.domain.repository.RecetaRepository;
import com.udo.can_cat.citas.application.service.CitaTransitionService;
import com.udo.can_cat.shared.impuestos.ImpuestosProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import static java.time.temporal.ChronoUnit.DAYS;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;

/**
 * CU 4.6.1.9 «Gestionar Atención Clínica» — núcleo transaccional.
 * Reglas de diseño implementadas: D2 (transiciones), D7 (agenda propia),
 * D8 (historial), D9 (propiedad de cita → 403), D10 (peso), D11 (entrada_historial),
 * D12 (factura de insumos sin pago), D14 (descuento atómico + movimiento).
 */
@Service
public class AtencionClinicaApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AtencionClinicaApplicationService.class);

    private static final String ESTADO_CONFIRMADA = "Confirmada";
    private static final String ESTADO_EN_ATENCION = "En_Atencion";
    private static final String ESTADO_COMPLETADA = "Completada";
    // private static final BigDecimal IVA_PORCENTAJE = new BigDecimal("16.00");
     private final ImpuestosProperties impuestos;

    private final AtencionClinicaRepository atencionRepo;
    private final AtencionInsumoRepository atencionInsumoRepo;
    private final RecetaRepository recetaRepo;
    private final RecetaItemRepository recetaItemRepo;
    private final EntradaHistorialRepository entradaHistorialRepo;
    private final ProductoRepository productoRepo;
    private final MovimientoInventarioRepository movimientoRepo;
    private final CitaPort citaPort;
    private final MascotaPort mascotaPort;
    private final PersonalPort personalPort;
    private final FacturacionPort facturacionPort;
    private final ClienteRepository clienteRepo;
    private final CitaTransitionService citaTransitionService;


    public AtencionClinicaApplicationService(AtencionClinicaRepository atencionRepo,
                                             AtencionInsumoRepository atencionInsumoRepo,
                                             RecetaRepository recetaRepo,
                                             RecetaItemRepository recetaItemRepo,
                                             EntradaHistorialRepository entradaHistorialRepo,
                                             ProductoRepository productoRepo,
                                             MovimientoInventarioRepository movimientoRepo,
                                             CitaPort citaPort,
                                             MascotaPort mascotaPort,
                                             PersonalPort personalPort,
                                             FacturacionPort facturacionPort,
                                             ClienteRepository clienteRepo,
                                             CitaTransitionService citaTransitionService,
                                            ImpuestosProperties impuestos) {
        this.atencionRepo = atencionRepo;
        this.atencionInsumoRepo = atencionInsumoRepo;
        this.recetaRepo = recetaRepo;
        this.recetaItemRepo = recetaItemRepo;
        this.entradaHistorialRepo = entradaHistorialRepo;
        this.productoRepo = productoRepo;
        this.movimientoRepo = movimientoRepo;
        this.citaPort = citaPort;
        this.mascotaPort = mascotaPort;
        this.personalPort = personalPort;
        this.facturacionPort = facturacionPort;
        this.clienteRepo = clienteRepo;
        this.citaTransitionService = citaTransitionService;
        this.impuestos = impuestos;
    }

    // ═══════════════════════════════════════════════════════════════════
    // D7 — AGENDA DEL VETERINARIO
    // ═══════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<CitaVetAgendaDTO> agendaDelVeterinario(LocalDate fecha) {
        PersonalPort.PersonalInfo personal = personalActual();
        LocalDate dia = (fecha != null) ? fecha : LocalDate.now();
        List<CitaPort.CitaAgendaInfo> citas = citaPort.buscarAgendaVeterinario(personal.personalId(), dia);
        if (citas.isEmpty()) return List.of();

        Set<Integer> atendidas = atencionRepo.buscarPorCitaIds(
                        citas.stream().map(CitaPort.CitaAgendaInfo::idCita).toList()).stream()
                .map(AtencionClinica::getIdCita)
                .collect(Collectors.toSet());

        return citas.stream()
                .map(c -> new CitaVetAgendaDTO(
                        c.idCita(), c.mascotaNombre(), c.especie(), c.raza(),
                        c.clienteNombre(), c.clienteDocumento(), c.clienteTelefono(),
                        c.motivoConsulta(), c.servicioNombre(),
                        c.fechaCita(), c.horaInicio(), c.horaFin(),
                        c.estadoNombre(), c.estadoColor(),
                        atendidas.contains(c.idCita())))
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════════
    // F2 — CONTEXTO DE LA ATENCIÓN
    // ═══════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public ContextoAtencionDTO contexto(Integer idCita) {
        PersonalPort.PersonalInfo personal = personalActual();
        CitaPort.CitaInfo cita = citaPort.buscarPorId(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException(idCita));
        validarPropiedad(cita, personal);
        MascotaPort.MascotaInfo mascota = mascotaPort.buscarPorId(cita.idMascota())
                .orElseThrow(() -> new MascotaNoEncontradaException(cita.idMascota()));

        // ¿Ya existe atención guardada para esta cita? → modo SOLO LECTURA (F2)
        AtencionClinica guardada = atencionRepo.buscarPorCitaId(idCita).orElse(null);
        AtencionHistorialDTO guardadaDTO =
                (guardada != null) ? cargarAtencionCompleta(guardada, personal.nombreCompleto()) : null;

        // Última atención PREVIA del paciente (excluye esta cita) para contexto clínico
        ContextoAtencionDTO.AtencionResumenDTO ultimaDTO = null;
        if (guardada == null) {
            AtencionClinica previa = atencionRepo.buscarPorMascotaId(mascota.idMascota()).stream()
                    .findFirst().orElse(null);
            if (previa != null) ultimaDTO = cargarAtencionResumen(previa);
        }

        return new ContextoAtencionDTO(
                new ContextoAtencionDTO.CitaDTO(cita.idCita(), cita.motivoConsulta(), cita.servicioNombre(),
                        cita.tipoAtencion(), cita.fechaCita(), cita.horaInicio(), cita.horaFin(),
                        cita.estadoNombre(), cita.costos()),
                new ContextoAtencionDTO.MascotaDTO(mascota.idMascota(), mascota.nombre(), mascota.especie(),
                        mascota.raza(), mascota.sexo(), mascota.fechaNacimiento(),
                        calcularEdad(mascota.fechaNacimiento()), mascota.pesoActual(),
                        mascota.esterilizado(), mascota.activo(), mascota.fallecido()),
                new ContextoAtencionDTO.DuenoDTO(mascota.idCliente(), mascota.clienteNombre(),
                        mascota.clienteDocumento(), mascota.clienteTelefono()),
                guardadaDTO,
                ultimaDTO,
                ultimaDTO == null && guardadaDTO == null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // D2 — INICIAR ATENCIÓN (Confirmada → En_Atencion, idempotente)
    // ═══════════════════════════════════════════════════════════════════
    @Transactional
    public void iniciarAtencion(Integer idCita) {
        PersonalPort.PersonalInfo personal = personalActual();

        // Idempotencia: si ya está En_Atencion, no se ejecuta transición
        CitaPort.CitaInfo cita = citaPort.buscarPorId(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException(idCita));
        if (ESTADO_EN_ATENCION.equals(cita.estadoNombre())) {
            log.info("Cita {} ya está En_Atención (iniciar es idempotente)", idCita);
            return;
        }

        citaTransitionService.transicionar(
            idCita,
            CitaTransitionService.EN_ATENCION,
            CitaTransitionService.Actor.veterinario(personal.personalId())
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // F7 — GUARDAR (TRANSACCIÓN ÚNICA; cualquier fallo → rollback total)
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public AtencionGuardadaResponseDTO guardar(GuardarAtencionRequestDTO request) {
        PersonalPort.PersonalInfo personal = personalActual();
        CitaPort.CitaInfo cita = citaPort.buscarPorId(request.idCita())
                .orElseThrow(() -> new CitaNoEncontradaException(request.idCita()));
        validarPropiedad(cita, personal);

        // (a) validaciones de cita — reglas 6.5.1 y 6.5.2
        if (atencionRepo.existePorCitaId(request.idCita())) {
            throw new AtencionYaRegistradaException();
        }
        if (!ESTADO_EN_ATENCION.equals(cita.estadoNombre())) {
            throw new OperacionAtencionInvalidaException(
                    "La cita debe estar En_Atención para guardar la consulta (estado actual: "
                            + cita.estadoNombre() + ")");
        }
        MascotaPort.MascotaInfo mascota = mascotaPort.buscarPorId(cita.idMascota())
                .orElseThrow(() -> new MascotaNoEncontradaException(cita.idMascota()));

        // (b) agrupar cantidades por producto + pre-validación de stock (regla 6.5.3)
        Map<Integer, Integer> cantidadesPorProducto = agrupar(request.insumos());
        Map<Integer, Producto> productos = cargarProductos(cantidadesPorProducto.keySet());
        for (Map.Entry<Integer, Integer> entry : cantidadesPorProducto.entrySet()) {
            Producto p = productos.get(entry.getKey());
            if (p == null || !Boolean.TRUE.equals(p.getActivo())) {
                throw new StockInsuficienteException("Producto #" + entry.getKey());
            }
            if (p.getStockActual() == null || p.getStockActual() < entry.getValue()) {
                throw new StockInsuficienteException(p.getNombre());
            }
        }

        // (c) INSERT atencion_clinica completa, 'Finalizada' (D2)
        AtencionClinica atencion = new AtencionClinica();
        atencion.setIdCita(cita.idCita());
        atencion.setIdVeterinario(personal.personalId());
        atencion.setIdMascota(cita.idMascota());
        atencion.setFechaHoraInicio(fechaHoraInicioCita(cita));
        atencion.setFechaHoraFin(LocalDateTime.now());
        atencion.setMotivoDetallado(cita.motivoConsulta());
        atencion.setAnamnesis(request.anamnesis());
        atencion.setSintomasObservados(request.sintomasObservados());
        atencion.setPesoKg(request.pesoKg());
        atencion.setTemperaturaC(request.temperaturaC());
        atencion.setFrecCardiaca(request.frecCardiaca());
        atencion.setFrecRespiratoria(request.frecRespiratoria());
        atencion.setDiagnosticoPrincipal(request.diagnosticoPrincipal());
        atencion.setDiagnosticosDiferenciales(request.diagnosticosDiferenciales());
        atencion.setObservacionesGenerales(request.observacionesGenerales());
        atencion.setIndicacionesDueno(request.indicacionesDueno() != null
                ? request.indicacionesDueno()
                : (request.receta() != null ? request.receta().indicacionesGenerales() : null));
        atencion.setProximaCitaRecomendada(request.proximaCitaRecomendada());
        atencion.setEstadoAtencion(AtencionClinica.ESTADO_FINALIZADA);
        try {
            atencion = atencionRepo.guardar(atencion);
        } catch (DataIntegrityViolationException dive) {
            // carrera sobre UNIQUE(id_cita): otro request guardó esta cita primero
            throw new AtencionYaRegistradaException();
        }

        // (d) por insumo: descuento atómico (D14) + snapshot + movimiento (D14)
        List<AtencionInsumo> insumosAplicados = new ArrayList<>();
        BigDecimal subtotalInsumos = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> entry : cantidadesPorProducto.entrySet()) {
            Producto p = productos.get(entry.getKey());
            int cantidad = entry.getValue();

            if (!productoRepo.descontarStock(p.getId(), cantidad)) {
                // alterno B — concurrencia real: rollback total
                throw new StockInsuficienteException(p.getNombre());
            }

            AtencionInsumo insumo = new AtencionInsumo();
            insumo.setIdAtencion(atencion.getId().value());
            insumo.setIdProducto(p.getId().value());
            insumo.setCantidad(cantidad);
            insumo.setPrecioUnitarioUsd(p.getPrecioVenta());
            insumosAplicados.add(insumo);
            subtotalInsumos = subtotalInsumos.add(
                    p.getPrecioVenta().multiply(BigDecimal.valueOf(cantidad)));

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setIdProducto(p.getId().value());
            movimiento.setIdPersonal(personal.personalId());
            movimiento.setTipoMovimiento(MovimientoInventario.TIPO_SALIDA);
            movimiento.setCantidad(cantidad);
            movimiento.setFechaMovimiento(LocalDateTime.now());
            movimiento.setMotivo(MovimientoInventario.MOTIVO_CONSUMO_CLINICA);
            movimiento.setDocumentoReferencia("ATN-" + atencion.getId().value());
            movimientoRepo.guardar(movimiento);
        }
        atencionInsumoRepo.guardarTodos(insumosAplicados);
        subtotalInsumos = subtotalInsumos.setScale(2, RoundingMode.HALF_UP);

        // (e) factura de productos SIN pago (D12)
        Integer idFactura = null;
        BigDecimal totalFactura = null;
        if (!insumosAplicados.isEmpty()) {
            LocalDate hoy = LocalDate.now();
            String numeroControl = generarCodigo("FC", facturacionPort.contarPorFecha(hoy) + 1, hoy);
            List<FacturacionPort.LineaInsumo> lineas = insumosAplicados.stream()
                    .map(i -> new FacturacionPort.LineaInsumo(
                            i.getIdProducto(),
                            productos.get(i.getIdProducto()).getNombre(),
                            i.getCantidad(),
                            i.getPrecioUnitarioUsd()))
                    .toList();
            FacturacionPort.FacturaCreada factura = facturacionPort.crearFacturaInsumos(
                    new FacturacionPort.FacturaInsumosNueva(
                            mascota.idCliente(), personal.personalId(), numeroControl,
                            subtotalInsumos, impuestos.getIvaPorcentaje(), lineas));
            idFactura = factura.idFactura();
            totalFactura = factura.totalNeto();
        }

        // (f) récipe digital (D5) — código RC-yyyyMMdd-NNNNNN
        String codigoReceta = null;
        Integer idReceta = null;
        if (request.receta() != null && !request.receta().items().isEmpty()) {
            LocalDate hoy = LocalDate.now();
            codigoReceta = generarCodigo("RC", recetaRepo.contarPorFecha(hoy) + 1, hoy);

            Receta receta = new Receta();
            receta.setIdAtencion(atencion.getId().value());
            receta.setCodigoReceta(codigoReceta);
            receta.setIndicacionesGenerales(request.receta().indicacionesGenerales());
            receta.setFechaEmision(LocalDateTime.now());
            receta = recetaRepo.guardar(receta);
            idReceta = receta.getId().value();

            List<RecetaItem> items = new ArrayList<>();
            List<GuardarAtencionRequestDTO.ItemRequestDTO> requestItems = request.receta().items();
            for (int i = 0; i < requestItems.size(); i++) {
                GuardarAtencionRequestDTO.ItemRequestDTO ri = requestItems.get(i);
                RecetaItem item = new RecetaItem();
                item.setIdReceta(idReceta);
                item.setMedicamento(ri.medicamento());
                item.setConcentracion(ri.concentracion());
                item.setDosis(ri.dosis());
                item.setViaAdministracion(ri.viaAdministracion());
                item.setFrecuencia(ri.frecuencia());
                item.setDuracion(ri.duracion());
                item.setOrden(i + 1);
                items.add(item);
            }
            recetaItemRepo.guardarTodos(items);
        }

        // (g) entrada al historial clínico permanente (D11)
        EntradaHistorial entrada = new EntradaHistorial();
        entrada.setIdMascota(cita.idMascota());
        entrada.setIdAtencion(atencion.getId().value());
        entrada.setTipoRegistro(EntradaHistorial.TIPO_CONSULTA);
        entrada.setFechaRegistro(LocalDateTime.now());
        entrada.setResumenEjecutivo(
                generarResumenEjecutivo(request.diagnosticoPrincipal(), insumosAplicados.size(), codigoReceta));
        entradaHistorialRepo.guardar(entrada);

        // (h) cita → Completada + peso de la mascota (D10)
        citaTransitionService.transicionar(
            cita.idCita(),
            CitaTransitionService.COMPLETADA,
            CitaTransitionService.Actor.veterinario(personal.personalId())
        );
        mascotaPort.actualizarPeso(cita.idMascota(), request.pesoKg());

        // regla 6.5.5 — log de resumen
        log.info("Atención guardada: idAtencion={}, cita={}, mascota={}, insumos={}, receta={}, factura={}",
                atencion.getId().value(), cita.idCita(), cita.idMascota(),
                insumosAplicados.size(), codigoReceta, idFactura);

        return new AtencionGuardadaResponseDTO(
                atencion.getId().value(), codigoReceta, idReceta, idFactura, totalFactura,
                AtencionGuardadaResponseDTO.MENSAJE_EXITO,
                new AtencionGuardadaResponseDTO.ResumenDTO(
                        mascota.nombre(), request.diagnosticoPrincipal(),
                        insumosAplicados.size(), subtotalInsumos, ESTADO_COMPLETADA));
    }

    // ═══════════════════════════════════════════════════════════════════
    // D8 — HISTORIAL CLÍNICO POR MASCOTA
    // ═══════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<AtencionHistorialDTO> historialPorMascota(Integer idMascota) {
        validarAccesoClienteSiCorresponde(idMascota);
        
        mascotaPort.buscarPorId(idMascota)
                .orElseThrow(() -> new MascotaNoEncontradaException(idMascota));
        List<AtencionClinica> atenciones = atencionRepo.buscarPorMascotaId(idMascota);
        if (atenciones.isEmpty()) return List.of();

        List<Integer> idsAtencion = atenciones.stream().map(a -> a.getId().value()).toList();
        Map<Integer, List<AtencionInsumo>> insumosPorAtencion = atencionInsumoRepo
                .buscarPorAtencionIds(idsAtencion).stream()
                .collect(Collectors.groupingBy(AtencionInsumo::getIdAtencion));
        Set<Integer> idsProducto = insumosPorAtencion.values().stream()
                .flatMap(List::stream).map(AtencionInsumo::getIdProducto).collect(Collectors.toSet());
        Map<Integer, Producto> productos = cargarProductos(idsProducto);

        Map<Integer, Receta> recetasPorAtencion = recetaRepo.buscarPorAtencionIds(idsAtencion).stream()
                .collect(Collectors.toMap(Receta::getIdAtencion, Function.identity()));
        Map<Integer, List<RecetaItem>> itemsPorReceta = new HashMap<>();
        for (Receta r : recetasPorAtencion.values()) {
            itemsPorReceta.put(r.getId().value(), recetaItemRepo.buscarPorRecetaId(r.getId().value()));
        }

        Map<Integer, String> nombresVeterinarios = new HashMap<>();
        for (AtencionClinica a : atenciones) {
            if (a.getIdVeterinario() != null && !nombresVeterinarios.containsKey(a.getIdVeterinario())) {
                nombresVeterinarios.put(a.getIdVeterinario(), personalPort.buscarPorId(a.getIdVeterinario())
                        .map(PersonalPort.PersonalInfo::nombreCompleto).orElse("—"));
            }
        }

        return atenciones.stream()
                .map(a -> {
                    Receta receta = recetasPorAtencion.get(a.getId().value());
                    return aHistorialDTO(
                            a,
                            insumosPorAtencion.getOrDefault(a.getId().value(), List.of()),
                            productos,
                            receta,
                            receta != null ? itemsPorReceta.getOrDefault(receta.getId().value(), List.of()) : List.of(),
                            nombresVeterinarios.getOrDefault(a.getIdVeterinario(), "—"));
                })
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════════
    // 6.2 — BÚSQUEDA DE PACIENTES (mascotas)
    // ═══════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<MascotaBusquedaDTO> buscarPacientes(String filtro) {
        if (filtro == null || filtro.isBlank()) return List.of();
        return mascotaPort.buscarPorFiltro(filtro.trim(), 20).stream()
                .map(m -> new MascotaBusquedaDTO(
                        m.idMascota(), m.nombre(), m.especie(), m.raza(), m.sexo(),
                        m.fechaNacimiento(), calcularEdad(m.fechaNacimiento()), m.pesoActual(),
                        m.activo(), m.fallecido(),
                        m.idCliente(), m.clienteNombre(), m.clienteDocumento(), m.clienteTelefono()))
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════

    /** Patrón 1.2: el Principal del SecurityContext es el usuarioId (Integer). */
    private PersonalPort.PersonalInfo personalActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Integer usuarioId)) {
            throw new AccessDeniedException("No hay un usuario autenticado válido");
        }
        return personalPort.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new AccessDeniedException(
                        "No existe personal activo asociado al usuario autenticado"));
    }

    /** D9: el veterinario solo opera SUS citas. */
    private void validarPropiedad(CitaPort.CitaInfo cita, PersonalPort.PersonalInfo personal) {
        if (cita.idVeterinario() == null || !cita.idVeterinario().equals(personal.personalId())) {
            throw new AccessDeniedException("Esta cita no pertenece al veterinario autenticado");
        }
    }

    /** D2: fecha_hora_inicio = fecha_cita + hora_inicio de la cita. */
    private LocalDateTime fechaHoraInicioCita(CitaPort.CitaInfo cita) {
        if (cita.horaInicio() != null && cita.fechaCita() != null) {
            return LocalDateTime.of(cita.fechaCita(), cita.horaInicio());
        }
        return (cita.fechaCita() != null) ? cita.fechaCita().atStartOfDay() : LocalDateTime.now();
    }

    /** Regla 6.5.3: el mismo producto repetido se suma en una sola línea. */
    private Map<Integer, Integer> agrupar(List<GuardarAtencionRequestDTO.InsumoRequestDTO> insumos) {
        if (insumos == null || insumos.isEmpty()) return Map.of();
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (GuardarAtencionRequestDTO.InsumoRequestDTO i : insumos) {
            cantidades.merge(i.idProducto(), i.cantidad(), Integer::sum);
        }
        return cantidades;
    }

    private Map<Integer, Producto> cargarProductos(Set<Integer> idsProducto) {
        if (idsProducto == null || idsProducto.isEmpty()) return Map.of();
        return productoRepo.buscarPorIds(idsProducto.stream().map(Producto.ProductoId::new).toList())
                .stream()
                .collect(Collectors.toMap(p -> p.getId().value(), Function.identity(), (a, b) -> a));
    }

    /** Códigos de comprobante: PREFIJO-yyyyMMdd-NNNNNN (1.3). */
    private String generarCodigo(String prefijo, long correlativo, LocalDate fecha) {
        return prefijo + "-" + fecha.format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + String.format("%06d", correlativo);
    }

    /** D11: "Consulta — Dx: <diagnóstico>. Insumos aplicados: N. Récipe: RC-..." */
    private String generarResumenEjecutivo(String diagnostico, int insumos, String codigoReceta) {
        StringBuilder sb = new StringBuilder("Consulta — Dx: ")
                .append(truncar(diagnostico, 300))
                .append(". Insumos aplicados: ").append(insumos);
        if (codigoReceta != null) {
            sb.append(". Récipe: ").append(codigoReceta);
        }
        return sb.toString();
    }

    private AtencionHistorialDTO cargarAtencionCompleta(AtencionClinica a, String veterinarioNombre) {
        List<AtencionInsumo> insumos = atencionInsumoRepo.buscarPorAtencionIds(List.of(a.getId().value()));
        Map<Integer, Producto> productos = cargarProductos(
                insumos.stream().map(AtencionInsumo::getIdProducto).collect(Collectors.toSet()));
        Receta receta = recetaRepo.buscarPorAtencionId(a.getId().value()).orElse(null);
        List<RecetaItem> items = (receta != null)
                ? recetaItemRepo.buscarPorRecetaId(receta.getId().value()) : List.of();
        return aHistorialDTO(a, insumos, productos, receta, items, veterinarioNombre);
    }

    private ContextoAtencionDTO.AtencionResumenDTO cargarAtencionResumen(AtencionClinica a) {
        List<AtencionInsumo> insumos = atencionInsumoRepo.buscarPorAtencionIds(List.of(a.getId().value()));
        Map<Integer, Producto> productos = cargarProductos(
                insumos.stream().map(AtencionInsumo::getIdProducto).collect(Collectors.toSet()));
        String insumosTexto = insumos.stream()
                .map(i -> (productos.containsKey(i.getIdProducto())
                        ? productos.get(i.getIdProducto()).getNombre()
                        : "#" + i.getIdProducto()) + " ×" + i.getCantidad())
                .collect(Collectors.joining(", "));
        String codigoReceta = recetaRepo.buscarPorAtencionId(a.getId().value())
                .map(Receta::getCodigoReceta).orElse(null);
        return new ContextoAtencionDTO.AtencionResumenDTO(
                a.getId().value(), a.getFechaHoraInicio(), a.getDiagnosticoPrincipal(),
                a.getObservacionesGenerales(), a.getIndicacionesDueno(), a.getPesoKg(),
                a.getTemperaturaC(), a.getFrecCardiaca(), insumosTexto, codigoReceta);
    }

    private AtencionHistorialDTO aHistorialDTO(AtencionClinica a, List<AtencionInsumo> insumos,
                                              Map<Integer, Producto> productos, Receta receta,
                                              List<RecetaItem> itemsReceta, String veterinarioNombre) {
        List<AtencionHistorialDTO.InsumoDTO> insumosDto = insumos.stream()
                .map(i -> {
                    Producto p = productos.get(i.getIdProducto());
                    return new AtencionHistorialDTO.InsumoDTO(
                            i.getIdProducto(),
                            p != null ? p.getNombre() : ("Producto #" + i.getIdProducto()),
                            p != null ? p.getCodigoSku() : null,
                            p != null ? p.getUnidadMedida() : null,
                            i.getCantidad(), i.getPrecioUnitarioUsd(),
                            i.getPrecioUnitarioUsd().multiply(BigDecimal.valueOf(i.getCantidad()))
                                    .setScale(2, RoundingMode.HALF_UP));
                }).toList();

        AtencionHistorialDTO.RecetaDTO recetaDto = null;
        if (receta != null) {
            recetaDto = new AtencionHistorialDTO.RecetaDTO(
                    receta.getId().value(), receta.getCodigoReceta(), receta.getIndicacionesGenerales(),
                    receta.getFechaEmision(),
                    itemsReceta.stream()
                            .map(it -> new AtencionHistorialDTO.ItemDTO(it.getMedicamento(), it.getConcentracion(),
                                    it.getDosis(), it.getViaAdministracion(), it.getFrecuencia(),
                                    it.getDuracion(), it.getOrden()))
                            .toList());
        }

        return new AtencionHistorialDTO(
                a.getId().value(), a.getIdCita(), a.getFechaHoraInicio(), a.getFechaHoraFin(),
                a.getEstadoAtencion(), veterinarioNombre,
                a.getAnamnesis(), a.getSintomasObservados(), a.getPesoKg(), a.getTemperaturaC(),
                a.getFrecCardiaca(), a.getFrecRespiratoria(),
                a.getDiagnosticoPrincipal(), a.getDiagnosticosDiferenciales(),
                a.getObservacionesGenerales(), a.getIndicacionesDueno(), a.getProximaCitaRecomendada(),
                insumosDto, recetaDto);
    }

    private String calcularEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return "—";
        LocalDate hoy = LocalDate.now();
        if (fechaNacimiento.isAfter(hoy)) return "—";
        Period p = Period.between(fechaNacimiento, hoy);
        if (p.getYears() > 0) {
            String texto = p.getYears() + (p.getYears() == 1 ? " año" : " años");
            if (p.getMonths() > 0) texto += " " + p.getMonths() + (p.getMonths() == 1 ? " mes" : " meses");
            return texto;
        }
        if (p.getMonths() > 0) return p.getMonths() + (p.getMonths() == 1 ? " mes" : " meses");
        long dias = DAYS.between(fechaNacimiento, hoy);
        return dias + (dias == 1 ? " día" : " días");
    }

    private String truncar(String texto, int max) {
        if (texto == null) return "";
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + "…";
    }

    private void validarAccesoClienteSiCorresponde(Integer idMascota) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esCliente = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_Cliente".equals(a.getAuthority()));
        if (!esCliente) return;

        Integer usuarioId = (auth.getPrincipal() instanceof Integer i) ? i : null;
        if (usuarioId == null) {
            throw new AccessDeniedException("No tiene permiso para acceder a esta mascota");
        }

        Integer idCliente = clienteRepo.findByUsuarioId(new UsuarioId(usuarioId))
                .map(c -> c.getId().value())
                .orElse(null);

        Integer idDueno = mascotaPort.buscarPorId(idMascota)
                .map(MascotaInfo::idCliente)
                .orElse(null);

        if (idCliente == null || idDueno == null || !idDueno.equals(idCliente)) {
            throw new AccessDeniedException("No tiene permiso para acceder a esta mascota");
        }
    }
}