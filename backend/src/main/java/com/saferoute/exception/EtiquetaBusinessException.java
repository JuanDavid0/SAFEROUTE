package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio en el módulo de Etiquetas.
 */
public class EtiquetaBusinessException extends RuntimeException {

    public EtiquetaBusinessException(String message) {
        super(message);
    }

    public EtiquetaBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
