package com.saferoute.exception;

/**
 * Excepción de negocio para el módulo de Reportes
 */
public class ReporteBusinessException extends RuntimeException {

    public ReporteBusinessException(String message) {
        super(message);
    }

    public ReporteBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
