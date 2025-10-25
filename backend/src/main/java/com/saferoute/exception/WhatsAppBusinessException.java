package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio en el módulo de WhatsApp.
 */
public class WhatsAppBusinessException extends RuntimeException {

    public WhatsAppBusinessException(String message) {
        super(message);
    }

    public WhatsAppBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
