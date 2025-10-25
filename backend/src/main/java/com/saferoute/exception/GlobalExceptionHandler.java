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
 * Manejador global de excepciones para toda la aplicación.
 * Captura todas las excepciones y las transforma en respuestas JSON
 * consistentes.
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

        /**
         * Excepciones específicas de negocio de Pedidos (400)
         */
        @ExceptionHandler(PedidoBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handlePedidoBusinessException(
                        PedidoBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Pedidos: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("PEDIDO_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de Pedido", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Solicitudes (400)
         */
        @ExceptionHandler(SolicitudBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleSolicitudBusinessException(
                        SolicitudBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Solicitudes: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("SOLICITUD_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de Solicitud", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Reportes (400)
         */
        @ExceptionHandler(ReporteBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleReporteBusinessException(
                        ReporteBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Reportes: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("REPORTE_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en generación de Reporte", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Excel (400)
         */
        @ExceptionHandler(ExcelBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleExcelBusinessException(
                        ExcelBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Excel: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("EXCEL_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de Excel", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de WhatsApp (400)
         */
        @ExceptionHandler(WhatsAppBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleWhatsAppBusinessException(
                        WhatsAppBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en WhatsApp: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("WHATSAPP_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de WhatsApp", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Etiquetas (400)
         */
        @ExceptionHandler(EtiquetaBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleEtiquetaBusinessException(
                        EtiquetaBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Etiquetas: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("ETIQUETA_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en generación de Etiqueta", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de OTP (400)
         */
        @ExceptionHandler(OtpBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleOtpBusinessException(
                        OtpBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en OTP: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("OTP_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de OTP", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Productos (400)
         */
        @ExceptionHandler(ProductoBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleProductoBusinessException(
                        ProductoBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Productos: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("PRODUCTO_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de Producto", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Autenticación (400)
         */
        @ExceptionHandler(AuthBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthBusinessException(
                        AuthBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Autenticación: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("AUTH_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de Autenticación", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Historial (400)
         */
        @ExceptionHandler(HistorialBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleHistorialBusinessException(
                        HistorialBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Historial: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("HISTORIAL_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de Historial", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Consolidación (400)
         */
        @ExceptionHandler(ConsolidacionBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleConsolidacionBusinessException(
                        ConsolidacionBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Consolidación: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("CONSOLIDACION_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de Consolidación", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Logs (400)
         */
        @ExceptionHandler(LogBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleLogBusinessException(
                        LogBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Logs: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("LOG_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de Log", error);
                response.setPath(request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Excepciones específicas de negocio de Usuarios (400)
         */
        @ExceptionHandler(UsuarioBusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleUsuarioBusinessException(
                        UsuarioBusinessException ex, HttpServletRequest request) {

                log.warn("Error de negocio en Usuarios: {}", ex.getMessage());

                ErrorDetails error = ErrorDetails.builder()
                                .code("USUARIO_BUSINESS_ERROR")
                                .details(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.fail("Error en operación de Usuario", error);
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
                                .details(String.format("El parámetro '%s' debe ser de tipo %s", ex.getName(),
                                                expectedType))
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
