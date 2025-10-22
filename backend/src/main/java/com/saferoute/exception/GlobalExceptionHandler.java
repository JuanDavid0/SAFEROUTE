package com.saferoute.exception;

import com.saferoute.dto.response.ApiResponse;
import com.saferoute.dto.response.ApiResponse.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API
 * Convierte todas las excepciones en respuestas JSON estandarizadas
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== EXCEPCIONES DE NEGOCIO PERSONALIZADAS
    // ====================

    /**
     * Recurso no encontrado (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Recurso no encontrado: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .code("RESOURCE_NOT_FOUND")
                .details(ex.getMessage())
                .build();

        ApiResponse<Void> response = ApiResponse.fail("Recurso no encontrado", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Operación no permitida / Regla de negocio violada (400)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Regla de negocio violada: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .code(ex.getErrorCode())
                .details(ex.getMessage())
                .build();

        ApiResponse<Void> response = ApiResponse.fail("Operación no permitida", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ==================== EXCEPCIONES DE VALIDACIÓN ====================

    /**
     * Validación de campos (@Valid) (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Error de validación en: {}", request.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorDetails error = ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .details("Uno o más campos tienen errores de validación")
                .build();

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .status("fail")
                .message("Error de validación")
                .data(errors)
                .error(error)
                .timestamp(java.time.LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Parámetro de request faltante (400)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Parámetro faltante: {}", ex.getParameterName());

        ErrorDetails error = ErrorDetails.builder()
                .code("MISSING_PARAMETER")
                .details(String.format("El parámetro '%s' es requerido", ex.getParameterName()))
                .field(ex.getParameterName())
                .build();

        ApiResponse<Void> response = ApiResponse.fail("Parámetro faltante", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Tipo de argumento incorrecto (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Tipo de dato incorrecto: {}", ex.getName());

        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

        ErrorDetails error = ErrorDetails.builder()
                .code("TYPE_MISMATCH")
                .details(String.format("El parámetro '%s' debe ser de tipo %s", ex.getName(), expectedType))
                .field(ex.getName())
                .rejectedValue(ex.getValue())
                .build();

        ApiResponse<Void> response = ApiResponse.fail("Tipo de dato incorrecto", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * JSON malformado (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("JSON inválido en request: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .code("INVALID_JSON")
                .details("El cuerpo de la petición no es un JSON válido")
                .build();

        ApiResponse<Void> response = ApiResponse.fail("JSON inválido", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ==================== EXCEPCIONES DE SEGURIDAD ====================

    /**
     * Credenciales incorrectas (401)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Intento de autenticación fallido en: {}", request.getRequestURI());

        ErrorDetails error = ErrorDetails.builder()
                .code("INVALID_CREDENTIALS")
                .details("Usuario o contraseña incorrectos")
                .build();

        ApiResponse<Void> response = ApiResponse.fail("Credenciales inválidas", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * No autenticado (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("Error de autenticación: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .code("AUTHENTICATION_FAILED")
                .details("Autenticación requerida")
                .build();

        ApiResponse<Void> response = ApiResponse.fail("No autenticado", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Acceso denegado (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Acceso denegado a: {}", request.getRequestURI());

        ErrorDetails error = ErrorDetails.builder()
                .code("ACCESS_DENIED")
                .details("No tienes permisos para acceder a este recurso")
                .build();

        ApiResponse<Void> response = ApiResponse.fail("Acceso denegado", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ==================== OTRAS EXCEPCIONES ====================

    /**
     * Endpoint no encontrado (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("Endpoint no encontrado: {}", request.getRequestURI());

        ErrorDetails error = ErrorDetails.builder()
                .code("ENDPOINT_NOT_FOUND")
                .details(String.format("El endpoint '%s' no existe", request.getRequestURI()))
                .build();

        ApiResponse<Void> response = ApiResponse.fail("Endpoint no encontrado", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Error genérico del servidor (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Error interno del servidor: ", ex);

        ErrorDetails error = ErrorDetails.builder()
                .code("INTERNAL_SERVER_ERROR")
                .details("Ha ocurrido un error interno. Por favor contacta al administrador.")
                .build();

        ApiResponse<Void> response = ApiResponse.error("Error interno del servidor", error);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
