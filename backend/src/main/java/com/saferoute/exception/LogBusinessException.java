package com.saferoute.exception;

/**
 * Excepción de negocio para operaciones relacionadas con Logs del sistema.
 * Se lanza cuando ocurren errores específicos del dominio de logs.
 */
public class LogBusinessException extends RuntimeException {

    public LogBusinessException(String message) {
        super(message);
    }

    public LogBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
