package com.saferoute.helper;

import com.saferoute.config.OtpConfig;
import com.saferoute.constants.OtpConstants;
import com.saferoute.exception.OtpBusinessException;
import com.saferoute.model.OtpToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Helper para validaciones de tokens OTP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtpTokenValidator {

    private final OtpConfig otpConfig;

    /**
     * Valida que el token OTP no haya expirado.
     *
     * @param token  Token a validar
     * @param cedula Cédula del usuario (para logging)
     * @throws OtpBusinessException si el token ha expirado
     */
    public void validarTokenNoExpirado(OtpToken token, String cedula) {
        if (token.haExpirado()) {
            log.warn(OtpConstants.LOG_TOKEN_OTP_EXPIRADO, cedula);
            throw new OtpBusinessException(
                    OtpConstants.ERROR_OTP_EXPIRADO,
                    OtpConstants.CODE_OTP_EXPIRED);
        }
    }

    /**
     * Valida que no se haya alcanzado el número máximo de intentos.
     *
     * @param token  Token a validar
     * @param cedula Cédula del usuario (para logging)
     * @throws OtpBusinessException si se alcanzó el máximo de intentos
     */
    public void validarMaximosIntentos(OtpToken token, String cedula) {
        if (token.getIntentosFallidos() >= otpConfig.getMaxAttempts()) {
            log.warn(OtpConstants.LOG_MAX_INTENTOS_ALCANZADO, cedula);
            throw new OtpBusinessException(
                    OtpConstants.ERROR_OTP_MAX_INTENTOS,
                    OtpConstants.CODE_OTP_MAX_ATTEMPTS);
        }
    }

    /**
     * Valida que el código OTP sea correcto.
     *
     * @param token     Token con el código esperado
     * @param codigoOtp Código ingresado por el usuario
     * @param cedula    Cédula del usuario (para logging)
     * @return true si el código es correcto
     */
    public boolean validarCodigoOtp(OtpToken token, String codigoOtp, String cedula) {
        if (!token.getCodigoOtp().equals(codigoOtp)) {
            int intentosRestantes = otpConfig.getMaxAttempts() - token.getIntentosFallidos();
            log.warn(OtpConstants.LOG_CODIGO_OTP_INCORRECTO, cedula, intentosRestantes);
            return false;
        }
        return true;
    }

    /**
     * Calcula el mensaje de error con intentos restantes.
     *
     * @param intentosFallidos Número de intentos fallidos actuales
     * @return Mensaje formateado con intentos restantes
     */
    public String obtenerMensajeIntentosRestantes(int intentosFallidos) {
        int intentosRestantes = otpConfig.getMaxAttempts() - intentosFallidos;
        return String.format(OtpConstants.ERROR_OTP_CODIGO_INCORRECTO, intentosRestantes);
    }
}
