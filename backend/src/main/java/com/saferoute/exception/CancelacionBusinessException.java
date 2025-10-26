package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio en Cancelación de Solicitudes
 */
public class CancelacionBusinessException extends RuntimeException {

    public CancelacionBusinessException(String message) {
        super(message);
    }

    public CancelacionBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
