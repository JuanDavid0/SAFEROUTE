package com.saferoute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginRequest {
    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[0-9]{8,10}$", message = "La cédula debe contener entre 8 y 10 dígitos numéricos")
    private String cedula;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenia;

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
}