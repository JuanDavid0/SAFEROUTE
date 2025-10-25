package com.saferoute.exception;

/**
 * Excepción de negocio para operaciones relacionadas con Usuarios.
 * Se lanza cuando ocurren errores específicos del dominio de usuarios.
 */
public class UsuarioBusinessException extends RuntimeException {

    public UsuarioBusinessException(String message) {
        super(message);
    }

    public UsuarioBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
