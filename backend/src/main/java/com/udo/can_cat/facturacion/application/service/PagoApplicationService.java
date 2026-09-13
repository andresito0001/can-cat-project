package com.udo.can_cat.facturacion.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.udo.can_cat.citas.domain.entity.Cita;
import com.udo.can_cat.citas.domain.entity.EstadoCita;
import com.udo.can_cat.citas.domain.entity.Servicio;
import com.udo.can_cat.citas.domain.exception.CitaNoEncontradaException;
import com.udo.can_cat.citas.domain.exception.OperacionNoPermitidaException;
import com.udo.can_cat.citas.domain.repository.CitaRepository;
import com.udo.can_cat.citas.domain.repository.EstadoCitaRepository;
import com.udo.can_cat.citas.domain.repository.ServicioRepository;
import com.udo.can_cat.facturacion.application.dto.HistorialPagoResponseDTO;
import com.udo.can_cat.facturacion.application.dto.MetodoPagoDTO;
import com.udo.can_cat.facturacion.application.dto.ProcesarPagoCitaRequestDTO;
import com.udo.can_cat.facturacion.application.dto.ProcesarPagoCitaResponseDTO;
import com.udo.can_cat.facturacion.domain.entity.DetalleFactura;
import com.udo.can_cat.facturacion.domain.entity.Factura;
import com.udo.can_cat.facturacion.domain.entity.MetodoPago;
import com.udo.can_cat.facturacion.domain.entity.Pago;
import com.udo.can_cat.facturacion.domain.exception.FacturacionException;
import com.udo.can_cat.facturacion.domain.repository.DetalleFacturaRepository;
import com.udo.can_cat.facturacion.domain.repository.FacturaRepository;
import com.udo.can_cat.facturacion.domain.repository.MetodoPagoRepository;
import com.udo.can_cat.facturacion.domain.repository.PagoRepository;
import com.udo.can_cat.mascotas.domain.entity.Mascota;
import com.udo.can_cat.mascotas.domain.repository.MascotaRepository;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import java.util.List;

@Service
public class PagoApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PagoApplicationService.class);
    private static final String PREFIJO_FACTURA = "FC";

    private final CitaRepository citaRepo;
    private final EstadoCitaRepository estadoCitaRepo;
    private final ServicioRepository servicioRepo;
    private final MascotaRepository mascotaRepo;
    private final ClienteRepository clienteRepo;
    private final PersonalRepository personalRepo;
    private final FacturaRepository facturaRepo;
    private final DetalleFacturaRepository detalleFacturaRepo;
    private final PagoRepository pagoRepo;
    private final MetodoPagoRepository metodoPagoRepo;
    private final UsuarioRepository usuarioRepo;

    public PagoApplicationService(CitaRepository citaRepo,
                                  EstadoCitaRepository estadoCitaRepo,
                                  ServicioRepository servicioRepo,
                                  MascotaRepository mascotaRepo,
                                  ClienteRepository clienteRepo,
                                  PersonalRepository personalRepo,     
                                  FacturaRepository facturaRepo,
                                  DetalleFacturaRepository detalleFacturaRepo,
                                  PagoRepository pagoRepo,
                                  MetodoPagoRepository metodoPagoRepo, 
                                  UsuarioRepository usuarioRepository) {
        this.citaRepo = citaRepo;
        this.estadoCitaRepo = estadoCitaRepo;
        this.servicioRepo = servicioRepo;
        this.mascotaRepo = mascotaRepo;
        this.clienteRepo = clienteRepo;
        this.personalRepo = personalRepo;                              
        this.facturaRepo = facturaRepo;
        this.detalleFacturaRepo = detalleFacturaRepo;
        this.pagoRepo = pagoRepo;
        this.metodoPagoRepo = metodoPagoRepo;
        this.usuarioRepo = usuarioRepository;
    }

    // ═══════════════════════════════════════════════════
    // LISTAR MÉTODOS DE PAGO ONLINE
    // ═══════════════════════════════════════════════════

    public java.util.List<MetodoPagoDTO> listarMetodosOnline() {
        // Solo Transferencia y Pago_Movil son viables para pago remoto
        return metodoPagoRepo.buscarActivos().stream()
                .filter(m -> "Transferencia".equals(m.getNombre()) || "Pago_Movil".equals(m.getNombre()))
                .map(m -> new MetodoPagoDTO(
                        m.getId(),
                        m.getNombre(),
                        m.getDescripcion(),
                        m.getDatosRequeridos() != null ? m.getDatosRequeridos() : Map.of()
                ))
                .toList();
    }

    // ═══════════════════════════════════════════════════
    // PROCESAR PAGO DE CITA (transaccional)
    // ═══════════════════════════════════════════════════

    @Transactional
    public ProcesarPagoCitaResponseDTO procesarPagoCita(ProcesarPagoCitaRequestDTO request) {
        // 1. Identificar cliente
        Integer idCliente = obtenerIdClienteActual();
        if (idCliente == null) {
            throw new OperacionNoPermitidaException("No se pudo identificar al cliente autenticado");
        }

        // 2. Buscar y validar cita
        Cita cita = citaRepo.buscarPorId(request.idCita())
                .orElseThrow(() -> new CitaNoEncontradaException("Cita no encontrada con ID: " + request.idCita()));

        String estadoActual = obtenerNombreEstado(cita.getIdEstado());
        if (!"Pendiente_Pago".equals(estadoActual)) {
            throw new OperacionNoPermitidaException(
                    "Solo se puede pagar citas en estado 'Pendiente_Pago'. Estado actual: " + estadoActual);
        }

        // Validar que la cita pertenece al cliente
        var clienteId = new Cliente.ClienteId(idCliente);
        boolean pertenece = mascotaRepo.findByClienteId(clienteId).stream()
                .anyMatch(m -> m.getId().value().equals(cita.getIdMascota()));
        if (!pertenece) {
            throw new OperacionNoPermitidaException("La cita no pertenece al cliente autenticado");
        }

        // 3. Validar método de pago
        MetodoPago metodoPago = metodoPagoRepo.buscarPorId(request.idMetodoPago())
                .orElseThrow(() -> new FacturacionException("Método de pago no encontrado"));

        // Validar campos requeridos del método
        validarDatosPago(metodoPago, request.datosPago());

        // 4. Buscar servicio para el detalle
        Servicio servicio = servicioRepo.buscarPorId(cita.getIdServicio())
                .filter(Servicio::getActivo)
                .orElseThrow(() -> new FacturacionException("Servicio de la cita no encontrado"));

        // 5. Generar número de control
        String numeroControl = generarNumeroControl();

        // 6. Crear factura
        Factura factura = new Factura();
        factura.setIdCliente(idCliente);
        factura.setIdCita(cita.getId());
        factura.setIdPersonal(cita.getIdVeterinario());
        factura.setNumeroControl(numeroControl);
        factura.setSubtotal(cita.getCostoUsd());
        factura.setPorcentajeDescuento(BigDecimal.ZERO);
        factura.setPorcentajeIva(new BigDecimal("16.00"));
        factura.setEstadoFactura("Emitida");
        factura.setMetodoPagoPrincipal(metodoPago.getNombre());
        factura = facturaRepo.guardar(factura);

        // 7. Crear detalle de factura
        DetalleFactura detalle = new DetalleFactura();
        detalle.setIdFactura(factura.getId());
        detalle.setTipoItem("Servicio_Consulta");
        detalle.setIdReferencia(cita.getIdServicio());
        detalle.setDescripcion(servicio.getNombre());
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(cita.getCostoUsd());
        detalle.setDescuentoAplicado(BigDecimal.ZERO);
        detalleFacturaRepo.guardar(detalle);

        // 8. Crear pago
        Pago pago = new Pago();
        pago.setIdFactura(factura.getId());
        pago.setIdMetodoPago(metodoPago.getId());
        pago.setIdCliente(idCliente);
        pago.setMonto(factura.getTotalNeto());
        pago.setFechaPago(java.time.LocalDateTime.now());
        pago.setReferenciaTransaccion(request.referenciaTransaccion());
        pago.setEstadoPago("Pendiente_Verificacion");
        pago.setMetadataJson(request.datosPago() != null ? request.datosPago().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Object) e.getValue(), (a, b) -> a, LinkedHashMap::new))
                : Map.of());
        pagoRepo.guardar(pago);

        // 9. Cambiar estado de cita a "Pagada"
        EstadoCita estadoPagada = estadoCitaRepo.buscarPorNombre("Pagada")
                .orElseThrow(() -> new OperacionNoPermitidaException("Estado 'Pagada' no encontrado"));
        cita.setIdEstado(estadoPagada.getId());
        citaRepo.guardar(cita);

        log.info("Pago procesado: cita={}, factura={}, numeroControl={}, metodo={}",
                cita.getId(), factura.getId(), numeroControl, metodoPago.getNombre());

        return new ProcesarPagoCitaResponseDTO(
                factura.getId(),
                numeroControl,
                "Pagada",
                "Su pago ha sido procesado exitosamente.",
                true,
                null
        );
    }

    // ═══════════════════════════════════════════════════
    // DATOS PARA GENERAR PDF
    // ═══════════════════════════════════════════════════

    public record DatosPdf(
            String numeroControl,
            String fechaEmision,
            String clienteNombre,
            String clienteDocumento,
            String clienteDireccion,
            String clienteCiudad,
            String clienteTelefono,
            String veterinarioNombre,
            String mascotaNombre,
            String motivoConsulta,
            String fechaCita,
            String horaInicio,
            java.util.List<DetalleFactura> detalles,
            BigDecimal subtotal,
            BigDecimal porcentajeIva,
            BigDecimal totalNeto,
            String metodoPago,
            String clienteEmail,
            String stadoPago 
    ) {}

    public DatosPdf obtenerDatosPdf(Integer idFactura) {
        Factura factura = facturaRepo.buscarPorId(idFactura)
                .orElseThrow(() -> new FacturacionException("Factura no encontrada: " + idFactura));

        java.util.List<DetalleFactura> detalles = detalleFacturaRepo.buscarPorFacturaId(idFactura);

        // FIX: cliente derivado de la FACTURA → funciona para cliente (dueño)
        // y para recepcionista (descarga/envío en mostrador).
        Cliente cliente = clienteRepo.findById(new Cliente.ClienteId(factura.getIdCliente()))
                .orElseThrow(() -> new FacturacionException("Cliente no encontrado"));

        String emailCliente = usuarioRepo.findById(cliente.getUsuarioId())
                .map(com.udo.can_cat.usuarios.domain.entity.Usuario::getCorreoElectronico)
                .orElse(null);

        String vetNombre = personalRepo.findById(new Personal.PersonalId(factura.getIdPersonal()))
                .map(Personal::getNombreCompleto)
                .orElse("No asignado");

        String estadoPago = pagoRepo.buscarPorFacturaId(idFactura)
                .map(Pago::getEstadoPago)
                .orElse(factura.getEstadoFactura());

        String mascotaNombre = "-";
        String motivoConsulta = "-";
        String fechaCita = "-";
        String horaInicio = "-";
        if (factura.getIdCita() != null) {
            try {
                Cita cita = citaRepo.buscarPorId(factura.getIdCita()).orElse(null);
                if (cita != null) {
                    motivoConsulta = cita.getMotivoConsulta();
                    fechaCita = cita.getFechaCita() != null ? cita.getFechaCita().toString() : "-";
                    horaInicio = cita.getHoraInicio() != null ? cita.getHoraInicio().toString() : "-";
                    Optional<Mascota> mascotaOpt = mascotaRepo
                            .findByClienteId(new Cliente.ClienteId(cliente.getId().value()))
                            .stream()
                            .filter(m -> m.getId().value().equals(cita.getIdMascota()))
                            .findFirst();
                    mascotaNombre = mascotaOpt.map(Mascota::getNombre).orElse("-");
                }
            } catch (Exception ignored) {}
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return new DatosPdf(
                factura.getNumeroControl(),
                factura.getFechaEmision() != null ? factura.getFechaEmision().format(dtf) : "-",
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getDireccion() != null ? cliente.getDireccion() : "-",
                cliente.getCiudad() != null ? cliente.getCiudad() : "-",
                cliente.getTelefonoPrincipal(),
                vetNombre,
                mascotaNombre,
                motivoConsulta,
                fechaCita,
                horaInicio,
                detalles,
                factura.getSubtotal(),
                factura.getPorcentajeIva() != null ? factura.getPorcentajeIva() : new BigDecimal("16.00"),
                factura.getTotalNeto(),
                factura.getMetodoPagoPrincipal(),
                emailCliente,
                estadoPago
        );
    }

    // ═══════════════════════════════════════════════════
    // HISTORIAL DE PAGOS DEL CLIENTE
    // ═══════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<HistorialPagoResponseDTO> obtenerHistorialPagos() {
        Integer idCliente = obtenerIdClienteActual();
        if (idCliente == null) {
            throw new OperacionNoPermitidaException("No se pudo identificar al cliente autenticado");
        }

        List<Factura> facturas = facturaRepo.buscarPorClienteId(idCliente);
        if (facturas.isEmpty()) return List.of();

        return facturas.stream().map(factura -> {
            // Concepto: descripción del primer detalle de la factura
            String concepto = detalleFacturaRepo.buscarPorFacturaId(factura.getId()).stream()
                    .findFirst()
                    .map(DetalleFactura::getDescripcion)
                    .orElse("Servicio veterinario");

            // Pago asociado (puede no existir si la factura vino de otro flujo)
            Pago pago = pagoRepo.buscarPorFacturaId(factura.getId()).orElse(null);

            // Fecha con fallback: fechaPago > fechaEmision > createdAt
            LocalDateTime fecha = null;
            if (pago != null && pago.getFechaPago() != null) {
                fecha = pago.getFechaPago();
            } else if (factura.getFechaEmision() != null) {
                fecha = factura.getFechaEmision();
            } else {
                fecha = factura.getCreatedAt();
            }

            return new HistorialPagoResponseDTO(
                    factura.getId(),
                    factura.getNumeroControl(),
                    fecha != null ? fecha.toString() : null,
                    concepto,
                    factura.getTotalNeto(),
                    pago != null ? pago.getEstadoPago() : factura.getEstadoFactura(),
                    factura.getMetodoPagoPrincipal(),
                    pago != null ? pago.getReferenciaTransaccion() : null,
                    factura.getIdCita()
            );
        }).toList();
    }

        // ═══════════════════════════════════════════════════
    // MÉTODOS DE PAGO PRESENCIALES (CU 4.6.1.11 paso 5)
    // ═══════════════════════════════════════════════════

    public List<MetodoPagoDTO> listarMetodosPresenciales() {
        // Presencial = todo menos Transferencia (Efectivo, Tarjeta, Pago_Movil)
        return metodoPagoRepo.buscarActivos().stream()
                .filter(m -> !"Transferencia".equals(m.getNombre()))
                .map(m -> new MetodoPagoDTO(
                        m.getId(),
                        m.getNombre(),
                        m.getDescripcion(),
                        m.getDatosRequeridos() != null ? m.getDatosRequeridos() : Map.of()
                ))
                .toList();
    }

    /**
     * CU 4.6.1.11: control de acceso a facturas.
     * Recepcionista (y roles internos): acceso total. Cliente: solo sus facturas.
     */
    public void validarAccesoFactura(Integer idFactura) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;
        boolean esCliente = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_Cliente".equals(a.getAuthority()));
        if (!esCliente) return;

        Integer idCliente = obtenerIdClienteActual();
        Factura factura = facturaRepo.buscarPorId(idFactura)
                .orElseThrow(() -> new FacturacionException("Factura no encontrada: " + idFactura));
        if (idCliente == null || !factura.getIdCliente().equals(idCliente)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No tiene acceso a esta factura");
        }
    }

    // ═══════════════════════════════════════════════════
    // PRIVADOS
    // ═══════════════════════════════════════════════════

    private String generarNumeroControl() {
        LocalDate hoy = LocalDate.now();
        String fechaStr = hoy.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long correlativo = facturaRepo.contarPorFecha(hoy) + 1;
        return String.format("%s-%s-%06d", PREFIJO_FACTURA, fechaStr, correlativo);
    }

    private void validarDatosPago(MetodoPago metodoPago, Map<String, String> datosPago) {
        Map<String, String> requeridos = metodoPago.getDatosRequeridos();
        if (requeridos == null || requeridos.isEmpty()) return;
        if (datosPago == null) {
            throw new FacturacionException("Debe proporcionar los datos requeridos para el método de pago: " + metodoPago.getNombre());
        }
        for (String campo : requeridos.keySet()) {
            String valor = datosPago.get(campo);
            if (valor == null || valor.isBlank()) {
                throw new FacturacionException("El campo '" + campo + "' es obligatorio para " + metodoPago.getNombre());
            }
        }
    }

    private Integer obtenerIdClienteActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Integer usuarioId;
        if (auth.getPrincipal() instanceof Integer) {
            usuarioId = (Integer) auth.getPrincipal();
        } else {
            return null;
        }
        return clienteRepo.findByUsuarioId(new UsuarioId(usuarioId))
                .map(c -> c.getId().value())
                .orElse(null);
    }

    private String obtenerNombreEstado(Integer idEstado) {
        return estadoCitaRepo.buscarTodos().stream()
                .filter(e -> e.getId().equals(idEstado))
                .findFirst()
                .map(EstadoCita::getNombre)
                .orElse("Desconocido");
    }
}