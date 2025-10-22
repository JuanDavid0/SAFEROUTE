package com.saferoute.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Clase genérica para respuestas API estandarizadas
 * Sigue el estándar JSend (https://github.com/omniti-labs/jsend)
 * 
 * @param <T> Tipo de datos a retornar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Estado de la respuesta: success, fail, error
     */
    private String status;

    /**
     * Mensaje descriptivo de la operación
     */
    private String message;

    /**
     * Datos de la respuesta (solo en caso de éxito)
     */
    private T data;

    /**
     * Detalles del error (solo en caso de fallo)
     */
    private ErrorDetails error;

    /**
     * Timestamp de la respuesta
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Path del endpoint solicitado
     */
    private String path;

    // ==================== MÉTODOS ESTÁTICOS DE CONSTRUCCIÓN ====================

    /**
     * Respuesta exitosa con datos
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Respuesta exitosa sin datos (ej: DELETE)
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Respuesta de fallo (errores de validación, negocio)
     */
    public static <T> ApiResponse<T> fail(String message, ErrorDetails error) {
        return ApiResponse.<T>builder()
                .status("fail")
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Respuesta de error (errores del sistema, excepciones)
     */
    public static <T> ApiResponse<T> error(String message, ErrorDetails error) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== CLASES INTERNAS ====================

    /**
     * Detalles del error
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {

        /**
         * Código de error específico
         */
        private String code;

        /**
         * Detalles técnicos del error
         */
        private String details;

        /**
         * Campo que causó el error (en validaciones)
         */
        private String field;

        /**
         * Valor rechazado (en validaciones)
         */
        private Object rejectedValue;
    }
}
