package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio en pedidos
 */
public class PedidoBusinessException extends RuntimeException {

    public PedidoBusinessException(String message) {
        super(message);
    }

    public PedidoBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
