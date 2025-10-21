package com.saferoute.dto;

/**
 * DTO de respuesta para operaciones OTP
 */
public class OtpResponse {

    private boolean exito;
    private String mensaje;
    private String token; // Token JWT temporal para modificar solicitud (solo si OTP verificado)

    // Constructores
    public OtpResponse() {
    }

    public OtpResponse(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
    }

    public OtpResponse(boolean exito, String mensaje, String token) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.token = token;
    }

    // Getters y Setters
    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
