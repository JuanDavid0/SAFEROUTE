package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio en el módulo de Excel.
 */
public class ExcelBusinessException extends RuntimeException {

    public ExcelBusinessException(String message) {
        super(message);
    }

    public ExcelBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
