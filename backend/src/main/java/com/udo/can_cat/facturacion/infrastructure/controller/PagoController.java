package com.udo.can_cat.facturacion.infrastructure.controller;

import com.udo.can_cat.facturacion.application.dto.*;
import com.udo.can_cat.facturacion.application.service.PagoApplicationService;
import com.udo.can_cat.facturacion.infrastructure.email.FacturaEmailService;
import com.udo.can_cat.facturacion.infrastructure.pdf.FacturaPdfGenerator;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoApplicationService pagoService;
    private final FacturaPdfGenerator pdfGenerator;
    private final FacturaEmailService emailService;
    private final ClienteRepository clienteRepo;
    private final UsuarioRepository usuarioRepo;

    public PagoController(PagoApplicationService pagoService,
                           FacturaPdfGenerator pdfGenerator,
                           FacturaEmailService emailService,
                           ClienteRepository clienteRepo,
                           UsuarioRepository usuarioRepo) {
        this.pagoService = pagoService;
        this.pdfGenerator = pdfGenerator;
        this.emailService = emailService;
        this.clienteRepo = clienteRepo;
        this.usuarioRepo = usuarioRepo;
    }

    /**
     * GET /api/pagos/metodos-online
     * Lista los métodos de pago disponibles para pago remoto.
     */
    @GetMapping("/metodos-online")
    public ResponseEntity<List<MetodoPagoDTO>> listarMetodosOnline() {
        return ResponseEntity.ok(pagoService.listarMetodosOnline());
    }

    /**
     * POST /api/pagos/procesar-pago-cita
     * Procesa el pago de una cita: crea factura + detalle + pago, cambia estado,
     * intenta enviar correo.
     */
    @PostMapping("/procesar-pago-cita")
    public ResponseEntity<ProcesarPagoCitaResponseDTO> procesarPagoCita(
            @Valid @RequestBody ProcesarPagoCitaRequestDTO request) {

        // 1. Procesar pago (transaccional)
        ProcesarPagoCitaResponseDTO respuesta = pagoService.procesarPagoCita(request);

        // 2. Post-procesamiento: generar PDF y enviar correo (fuera de la transacción)
        try {
            var datosPdf = pagoService.obtenerDatosPdf(respuesta.idFactura());
            byte[] pdfBytes = pdfGenerator.generar(datosPdf);

            // Obtener correo del cliente
            Integer idUsuario = obtenerIdUsuarioActual();
            String emailCliente = null;
            String nombreCliente = "";

            if (idUsuario != null) {
                // Buscar usuario por ID para obtener el email
                var usuario = usuarioRepo.findById(new UsuarioId(idUsuario)).orElse(null);
                if (usuario != null) {
                    emailCliente = usuario.getCorreoElectronico();
                }

                // Buscar cliente asociado para obtener nombre preferido del cliente (si aplica)
                var cliente = clienteRepo.findByUsuarioId(new UsuarioId(idUsuario)).orElse(null);
                if (cliente != null && cliente.getNombreCompleto() != null && !cliente.getNombreCompleto().isBlank()) {
                    nombreCliente = cliente.getNombreCompleto();
                }
            }

            boolean enviado = false;
            String advertencia = null;

            if (emailCliente != null && !emailCliente.isBlank()) {
                enviado = emailService.enviarFactura(emailCliente, pdfBytes, respuesta.numeroControl(), nombreCliente);
                if (!enviado) {
                    advertencia = "Su pago y factura fueron procesados con éxito, pero hubo un problema técnico al enviar la copia a su correo. Por favor, descárguela directamente desde esta pantalla.";
                }
            } else {
                advertencia = "No se encontró un correo electrónico registrado. Por favor, descargue su factura directamente desde esta pantalla.";
            }

            return ResponseEntity.ok(new ProcesarPagoCitaResponseDTO(
                    respuesta.idFactura(),
                    respuesta.numeroControl(),
                    respuesta.estadoCita(),
                    respuesta.mensaje(),
                    enviado,
                    advertencia
            ));

        } catch (Exception e) {
            // Si falla el post-procesamiento, el pago ya quedó registrado. Retornamos con advertencia.
            return ResponseEntity.ok(new ProcesarPagoCitaResponseDTO(
                    respuesta.idFactura(),
                    respuesta.numeroControl(),
                    respuesta.estadoCita(),
                    respuesta.mensaje(),
                    false,
                    "Su pago fue procesado pero no se pudo generar el comprobante PDF en este momento. Puede descargarlo más tarde desde su historial de pagos."
            ));
        }
    }

    /**
     * GET /api/pagos/facturas/{id}/descargar
     * Genera y descarga el PDF de una factura.
     */
    @GetMapping("/facturas/{id}/descargar")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable Integer id) {
        pagoService.validarAccesoFactura(id);
        var datos = pagoService.obtenerDatosPdf(id);
        byte[] pdf = pdfGenerator.generar(datos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Factura-" + datos.numeroControl() + ".pdf");
        headers.setContentLength(pdf.length);

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    /**
     * GET /api/pagos/historial
     * Retorna el historial de pagos y facturas del cliente autenticado,
     * ordenado de la más reciente a la más antigua.
     */
    @GetMapping("/historial")
    public ResponseEntity<List<HistorialPagoResponseDTO>> obtenerHistorialPagos() {
        return ResponseEntity.ok(pagoService.obtenerHistorialPagos());
    }


        /**
     * GET /api/pagos/metodos-presenciales
     * CU 4.6.1.11 paso 5: Efectivo, Tarjeta (punto), Pago_Movil.
     */
    @GetMapping("/metodos-presenciales")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('Recepcionista')")
    public ResponseEntity<List<MetodoPagoDTO>> listarMetodosPresenciales() {
        return ResponseEntity.ok(pagoService.listarMetodosPresenciales());
    }

    /**
     * POST /api/pagos/facturas/{id}/enviar
     * CU 4.6.1.11 paso 8: envío del comprobante "si el cliente lo solicita".
     */
    @PostMapping("/facturas/{id}/enviar")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('Recepcionista')")
    public ResponseEntity<EnvioFacturaResponseDTO> enviarFactura(@PathVariable Integer id) {
        var datos = pagoService.obtenerDatosPdf(id);
        byte[] pdf = pdfGenerator.generar(datos);

        if (datos.clienteEmail() == null || datos.clienteEmail().isBlank()) {
            return ResponseEntity.ok(new EnvioFacturaResponseDTO(false,
                    "El cliente no tiene correo electrónico registrado."));
        }
        boolean ok = emailService.enviarFactura(
                datos.clienteEmail(), pdf, datos.numeroControl(), datos.clienteNombre());
        return ResponseEntity.ok(new EnvioFacturaResponseDTO(ok, ok
                ? "Factura enviada a " + datos.clienteEmail()
                : "No se pudo enviar el correo. Intente nuevamente o entregue la copia impresa."));
    }

    private Integer obtenerIdUsuarioActual() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        if (auth.getPrincipal() instanceof Integer id) {
            return id; // este es el ID del usuario
        }
        // Si el principal es otro tipo (por ejemplo, tu UserDetails custom), ajústalo aquí
        return null;
    }
}