package com.udo.can_cat.config;

import com.udo.can_cat.citas.domain.exception.CitaNoEncontradaException;
import com.udo.can_cat.citas.domain.exception.HorarioNoDisponibleException;
import com.udo.can_cat.citas.domain.exception.MascotaNoPerteneceAlClienteException;
import com.udo.can_cat.citas.domain.exception.OperacionNoPermitidaException;
import com.udo.can_cat.mascotas.application.service.MascotaApplicationService;
import com.udo.can_cat.shared.tasa.TasaCambioException;
import com.udo.can_cat.usuarios.application.service.CredencialesInvalidasException;
import com.udo.can_cat.usuarios.application.service.CuentaInactivaException;
import com.udo.can_cat.usuarios.application.service.RegistroException;
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