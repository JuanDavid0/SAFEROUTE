package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio del módulo Historial.
 */
public class HistorialBusinessException extends RuntimeException {

    public HistorialBusinessException(String message) {
        super(message);
    }

    public HistorialBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
