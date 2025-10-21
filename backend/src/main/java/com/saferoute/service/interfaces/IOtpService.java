package com.saferoute.service.interfaces;

import com.saferoute.dto.OtpResponse;

/**
 * Interfaz del servicio de autenticación OTP para clientes
 */
public interface IOtpService {

    /**
     * Genera y envía un código OTP al teléfono asociado con la cédula
     * 
     * @param cedula Cédula del cliente
     * @return Respuesta indicando si el OTP fue enviado exitosamente
     */
    OtpResponse solicitarOtp(String cedula);

    /**
     * Verifica el código OTP ingresado por el cliente
     * 
     * @param cedula    Cédula del cliente
     * @param codigoOtp Código OTP ingresado
     * @return Respuesta con token JWT temporal si la verificación es exitosa
     */
    OtpResponse verificarOtp(String cedula, String codigoOtp);

    /**
     * Limpia tokens OTP expirados de la base de datos
     */
    void limpiarTokensExpirados();
}
