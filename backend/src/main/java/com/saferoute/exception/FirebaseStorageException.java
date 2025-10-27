package com.saferoute.exception;

/**
 * Excepción de negocio para operaciones de Firebase Storage
 */
public class FirebaseStorageException extends RuntimeException {

    public FirebaseStorageException(String message) {
        super(message);
    }

    public FirebaseStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
