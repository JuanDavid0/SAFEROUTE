package com.saferoute.exception;

/**
 * Excepción personalizada para errores de negocio del módulo OTP
 */
public class OtpBusinessException extends RuntimeException {

    private final String errorCode;

    public OtpBusinessException(String message) {
        super(message);
        this.errorCode = "OTP_ERROR";
    }

    public OtpBusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public OtpBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "OTP_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
