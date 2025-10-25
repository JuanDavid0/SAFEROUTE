package com.saferoute.helper;

import com.saferoute.constants.OtpConstants;
import org.springframework.stereotype.Component;

/**
 * Helper para formateo de mensajes relacionados con OTP.
 */
@Component
public class OtpMessageFormatter {

    /**
     * Formatea el mensaje de confirmación de envío de OTP ocultando parte del
     * teléfono.
     *
     * @param telefono Número de teléfono completo
     * @return Mensaje formateado con últimos dígitos
     */
    public String formatearMensajeOtpEnviado(String telefono) {
        String ultimosDigitos = obtenerUltimosDigitos(telefono);
        return String.format(OtpConstants.MENSAJE_OTP_ENVIADO, ultimosDigitos);
    }

    /**
     * Obtiene los últimos N dígitos del número de teléfono.
     *
     * @param telefono Número de teléfono completo
     * @return Últimos dígitos del teléfono
     */
    private String obtenerUltimosDigitos(String telefono) {
        if (telefono == null || telefono.length() < OtpConstants.LONGITUD_ULTIMOS_DIGITOS_TELEFONO) {
            return telefono;
        }
        return telefono.substring(telefono.length() - OtpConstants.LONGITUD_ULTIMOS_DIGITOS_TELEFONO);
    }
}
