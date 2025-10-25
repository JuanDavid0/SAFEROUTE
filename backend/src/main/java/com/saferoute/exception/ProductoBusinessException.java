package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio del módulo Producto.
 */
public class ProductoBusinessException extends RuntimeException {

    public ProductoBusinessException(String message) {
        super(message);
    }

    public ProductoBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
