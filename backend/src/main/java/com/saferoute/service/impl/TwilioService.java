package com.saferoute.service.impl;

import com.saferoute.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Servicio para envío de SMS a través de Twilio
 */
@Service
public class TwilioService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioService.class);

    private final TwilioConfig twilioConfig;

    public TwilioService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    /**
     * Inicializa Twilio con las credenciales configuradas
     */
    @PostConstruct
    public void initTwilio() {
        try {
            Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
            logger.info("Twilio inicializado correctamente");
        } catch (Exception e) {
            logger.error("Error al inicializar Twilio: {}", e.getMessage());
        }
    }

    /**
     * Envía un SMS con el código OTP al número de teléfono especificado
     * 
     * @param telefono  Número de teléfono del destinatario (formato: +57XXXXXXXXXX)
     * @param codigoOtp Código OTP de 6 dígitos
     * @return true si el SMS se envió exitosamente, false en caso contrario
     */
    public boolean enviarSmsOtp(String telefono, String codigoOtp) {
        try {
            // Formatear número de teléfono (agregar +57 si no lo tiene)
            String numeroFormateado = formatearNumeroTelefono(telefono);

            String mensaje = String.format(
                    "SafeRoute: Tu código de verificación es: %s. Válido por 5 minutos. No compartas este código.",
                    codigoOtp);

            Message message = Message.creator(
                    new PhoneNumber(numeroFormateado),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    mensaje).create();

            logger.info("SMS OTP enviado exitosamente a {}. SID: {}", numeroFormateado, message.getSid());
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar SMS OTP a {}: {}", telefono, e.getMessage());
            return false;
        }
    }

    /**
     * Formatea el número de teléfono al formato internacional colombiano
     * 
     * @param telefono Número de teléfono de 10 dígitos
     * @return Número formateado con código de país (+57)
     */
    private String formatearNumeroTelefono(String telefono) {
        // Si ya tiene el código de país, devolverlo tal cual
        if (telefono.startsWith("+")) {
            return telefono;
        }
        // Agregar código de país de Colombia (+57)
        return "+57" + telefono;
    }
}
