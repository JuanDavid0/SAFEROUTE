package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio del módulo Consolidación.
 */
public class ConsolidacionBusinessException extends RuntimeException {

    public ConsolidacionBusinessException(String message) {
        super(message);
    }

    public ConsolidacionBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
