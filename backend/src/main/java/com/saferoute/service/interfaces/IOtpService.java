package com.saferoute.service.interfaces;

import com.saferoute.dto.OtpResponse;

/**
 * Interfaz del servicio de autenticación OTP para clientes
 */
public interface IOtpService {

    OtpResponse solicitarOtp(String cedula);

    OtpResponse verificarOtp(String cedula, String codigoOtp);

    void limpiarTokensExpirados();
}
