package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio en solicitudes
 */
public class SolicitudBusinessException extends RuntimeException {

    public SolicitudBusinessException(String message) {
        super(message);
    }

    public SolicitudBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
