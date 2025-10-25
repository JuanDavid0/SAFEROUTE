package com.saferoute.exception;

/**
 * Excepción personalizada para errores de autenticación y autorización.
 */
public class AuthBusinessException extends RuntimeException {

    public AuthBusinessException(String message) {
        super(message);
    }

    public AuthBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
