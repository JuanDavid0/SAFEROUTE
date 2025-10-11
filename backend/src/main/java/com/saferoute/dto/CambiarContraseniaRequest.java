package com.saferoute.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CambiarContraseniaRequest {

    @NotBlank(message = "El correo es requerido")
    @Email(message = "El formato del correo no es válido")
    private String correo;

    @NotBlank(message = "La contraseña actual es requerida")
    private String contraseniaActual;

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    private String contraseniaNueva;

    // Constructores
    public CambiarContraseniaRequest() {
    }

    public CambiarContraseniaRequest(String correo, String contraseniaActual, String contraseniaNueva) {
        this.correo = correo;
        this.contraseniaActual = contraseniaActual;
        this.contraseniaNueva = contraseniaNueva;
    }

    // Getters y Setters
    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContraseniaActual() {
        return contraseniaActual;
    }

    public void setContraseniaActual(String contraseniaActual) {
        this.contraseniaActual = contraseniaActual;
    }

    public String getContraseniaNueva() {
        return contraseniaNueva;
    }

    public void setContraseniaNueva(String contraseniaNueva) {
        this.contraseniaNueva = contraseniaNueva;
    }
}