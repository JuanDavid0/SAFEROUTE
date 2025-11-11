package com.saferoute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para solicitar un código OTP
 */
public class SolicitarOtpRequest {

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[0-9]{8,10}$", message = "La cédula debe contener entre 8 y 10 dígitos numéricos")
    private String cedula;

    // Getters y Setters
    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }
}
