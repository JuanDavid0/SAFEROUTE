package com.saferoute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para verificar un código OTP
 */
public class VerificarOtpRequest {

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe contener exactamente 10 dígitos numéricos")
    private String cedula;

    @NotBlank(message = "El código OTP es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código OTP debe contener exactamente 6 dígitos")
    private String codigoOtp;

    // Getters y Setters
    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getCodigoOtp() {
        return codigoOtp;
    }

    public void setCodigoOtp(String codigoOtp) {
        this.codigoOtp = codigoOtp;
    }
}
