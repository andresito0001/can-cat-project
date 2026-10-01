package com.udo.can_cat.config;

import com.udo.can_cat.facturacion.domain.exception.FacturaYaPagadaException;
import com.udo.can_cat.facturacion.domain.exception.FacturacionException;
import org.springframework.security.access.AccessDeniedException;
import com.udo.can_cat.citas.domain.exception.CitaNoEncontradaException;
import com.udo.can_cat.citas.domain.exception.HorarioNoDisponibleException;
import com.udo.can_cat.citas.domain.exception.MascotaNoPerteneceAlClienteException;
import com.udo.can_cat.citas.domain.exception.OperacionNoPermitidaException;
import com.udo.can_cat.mascotas.application.service.MascotaApplicationService;
import com.udo.can_cat.shared.tasa.TasaCambioException;
import com.udo.can_cat.usuarios.application.service.ContrasenaActualIncorrectaException;
import com.udo.can_cat.usuarios.application.service.CredencialesInvalidasException;
import com.udo.can_cat.usuarios.application.service.CuentaInactivaException;
import com.udo.can_cat.usuarios.application.service.RegistroException;
import com.udo.can_cat.usuarios.application.service.TokenRecuperacionInvalidoException;
import com.udo.can_cat.almacen.domain.exception.EntradaInvalidaException;
import com.udo.can_cat.almacen.domain.exception.ProductoNoEncontradoException;
import com.udo.can_cat.almacen.domain.exception.ProveedorNoEncontradoException;
import com.udo.can_cat.almacen.domain.exception.SkuDuplicadoException;
import com.udo.can_cat.almacen.domain.exception.StockInsuficienteException;
import com.udo.can_cat.atenciones.domain.exception.AtencionYaRegistradaException;
import com.udo.can_cat.atenciones.domain.exception.MascotaNoEncontradaException;
import com.udo.can_cat.atenciones.domain.exception.OperacionAtencionInvalidaException;
import com.udo.can_cat.atenciones.domain.exception.RecetaNoEncontradaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import com.udo.can_cat.citas.domain.exception.TransicionInvalidaException;
import com.udo.can_cat.usuarios.application.service.TokenRecuperacionInvalidoException;
import com.udo.can_cat.usuarios.application.service.ContrasenaActualIncorrectaException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ================================================================
    // MÓDULO USUARIOS
    // ================================================================

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        logger.warn("Credenciales inválidas: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(),
                "Credenciales incorrectas", ex.getMessage()));
    }

    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaInactiva(CuentaInactivaException ex) {
        logger.warn("Cuenta inactiva: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.FORBIDDEN.value(),
                "Cuenta inactiva", ex.getMessage()));
    }

    @ExceptionHandler(RegistroException.class)
    public ResponseEntity<ErrorResponse> handleRegistroException(RegistroException ex) {
        logger.warn("Error de registro: {} - {}", ex.getCodigo(), ex.getMessage());
        HttpStatus status = switch (ex.getCodigo()) {
            case "CORREO_DUPLICADO", "DOCUMENTO_DUPLICADO" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(new ErrorResponse(
                LocalDateTime.now(), status.value(), ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(TokenRecuperacionInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleTokenRecuperacionInvalido(TokenRecuperacionInvalidoException ex) {
        logger.warn("Token de recuperación inválido: {} - {}", ex.getCodigo(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getCodigo(),
                ex.getMessage()));
    }

    // ================================================================
    // MÓDULO MASCOTAS
    // ================================================================

    @ExceptionHandler(MascotaApplicationService.MascotaRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleMascotaRegistration(
            MascotaApplicationService.MascotaRegistrationException ex) {
        logger.warn("Error de registro de mascota: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Error de registro de mascota", ex.getMessage()));
    }

    // ================================================================
    // MÓDULO CITAS
    // ================================================================

    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleCitaNoEncontrada(CitaNoEncontradaException ex) {
        logger.warn("Cita no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                "Cita no encontrada", ex.getMessage()));
    }

    @ExceptionHandler(MascotaNoPerteneceAlClienteException.class)
    public ResponseEntity<ErrorResponse> handleMascotaNoPertenece(MascotaNoPerteneceAlClienteException ex) {
        logger.warn("Mascota no pertenece al cliente: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.FORBIDDEN.value(),
                "Acceso denegado", ex.getMessage()));
    }

    @ExceptionHandler(HorarioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleHorarioNoDisponible(HorarioNoDisponibleException ex) {
        logger.warn("Horario no disponible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                "Horario no disponible", ex.getMessage()));
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ErrorResponse> handleOperacionNoPermitida(OperacionNoPermitidaException ex) {
        logger.warn("Operación no permitida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                "Operación no permitida", ex.getMessage()));
    }

    // ================================================================
    // SHARED — TASA DE CAMBIO
    // ================================================================

    @ExceptionHandler(TasaCambioException.class)
    public ResponseEntity<ErrorResponse> handleTasaCambio(TasaCambioException ex) {
        logger.error("Error de tasa de cambio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Servicio no disponible", ex.getMessage()));
    }

    // ================================================================
    // VALIDACIÓN
    // ================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Error de validación", errors.toString()));
    }

    // ================================================================
    // FACTURACIÓN / SEGURIDAD
    // ================================================================

    @ExceptionHandler(FacturacionException.class)
    public ResponseEntity<ErrorResponse> handleFacturacion(FacturacionException ex) {
        logger.warn("Error de facturación: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Error de facturación", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.FORBIDDEN.value(),
                "Acceso denegado", "No tiene permisos para realizar esta operación"));
    }

    // ================================================================
    // MÓDULO ATENCIONES / ALMACÉN (CU 4.6.1.9)
    // ⚠️ Nombres FQN donde hay colisión de nombre simple con citas.
    // ================================================================

    /** Alterno B (concurrencia): rollback total + 409 (5.2-B). */
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleStockInsuficiente(StockInsuficienteException ex) {
        logger.warn("Stock insuficiente: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(LocalDateTime.now(), 409, "Conflict", ex.getMessage()));
    }

    /** Alterno D: cita ya atendida → la vista entra en modo solo lectura. */
    @ExceptionHandler(AtencionYaRegistradaException.class)
    public ResponseEntity<ErrorResponse> handleAtencionYaRegistrada(AtencionYaRegistradaException ex) {
        logger.warn("Atención ya registrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(LocalDateTime.now(), 409, "Conflict", ex.getMessage()));
    }

    /** Estado de cita inválido para la operación (iniciar/guardar) → 409. */
    @ExceptionHandler(OperacionAtencionInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleOperacionAtencionInvalida(OperacionAtencionInvalidaException ex) {
        logger.warn("Operación de atención inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(LocalDateTime.now(), 409, "Conflict", ex.getMessage()));
    }

    /** Récipe no encontrado → 404. */
    @ExceptionHandler(RecetaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleRecetaNoEncontrada(RecetaNoEncontradaException ex) {
        logger.warn("Récipe no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(LocalDateTime.now(), 404, "Not Found", ex.getMessage()));
    }

    /** Paciente (mascota) no encontrado → 404. */
    @ExceptionHandler(MascotaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleMascotaNoEncontradaAtenciones(MascotaNoEncontradaException ex) {
        logger.warn("Paciente no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(LocalDateTime.now(), 404, "Not Found", ex.getMessage()));
    }

    /** Cita no encontrada desde el módulo ATENCIONES → 404.
     *  FQN obligatorio: el nombre simple ya está importado desde citas. */
    @ExceptionHandler(com.udo.can_cat.atenciones.domain.exception.CitaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleCitaNoEncontradaAtenciones(
            com.udo.can_cat.atenciones.domain.exception.CitaNoEncontradaException ex) {
        logger.warn("Cita no encontrada (atenciones): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(LocalDateTime.now(), 404, "Not Found", ex.getMessage()));
    }
    
    
    @ExceptionHandler(FacturaYaPagadaException.class)
    public ResponseEntity<ErrorResponse> handleFacturaYaPagada(FacturaYaPagadaException ex) {
        logger.warn("Factura ya pagada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(LocalDateTime.now(), 409, "Conflict", ex.getMessage()));
    }

    // ================================================================
    // MÓDULO ALMACÉN — CU 4.6.1.12
    // ================================================================

    @ExceptionHandler(EntradaInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleEntradaInvalida(EntradaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Datos inválidos", ex.getMessage()));
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleProductoNoEncontrado(ProductoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Recurso no encontrado", ex.getMessage()));
    }

    @ExceptionHandler(SkuDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleSkuDuplicado(SkuDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Conflicto de datos", ex.getMessage()));
    }

    @ExceptionHandler(ProveedorNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleProveedorNoEncontrado(ProveedorNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Recurso no encontrado", ex.getMessage()));
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleTransicionInvalida(TransicionInvalidaException ex) {
        logger.warn("Transición inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                "Transición no permitida", ex.getMessage()));
    }
    
    /* ═══════════════════════════════════════════════════════════════
    MÓDULO PERFIL
    ═══════════════════════════════════════════════════════════════ */

    @ExceptionHandler(ContrasenaActualIncorrectaException.class)
    public ResponseEntity<ErrorResponse> handleContrasenaActualIncorrecta(
            ContrasenaActualIncorrectaException ex) {
        logger.warn("Contraseña actual incorrecta al cambiar contraseña");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Contraseña incorrecta", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Argumento inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Solicitud inválida", ex.getMessage()));
    }

    // ================================================================
    // CATCH-ALL — DEBE SER EL ÚLTIMO
    // ================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Error no controlado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor", "Ha ocurrido un error inesperado"));
    }

    // ================================================================
    // RECORD DE RESPUESTA
    // ================================================================

    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message
    ) {}
}