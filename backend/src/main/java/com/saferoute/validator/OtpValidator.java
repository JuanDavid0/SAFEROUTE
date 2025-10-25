package com.saferoute.validator;

import com.saferoute.constants.OtpConstants;
import com.saferoute.exception.OtpBusinessException;
import com.saferoute.model.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validador para operaciones relacionadas con OTP.
 */
@Slf4j
@Component
public class OtpValidator {

    /**
     * Valida que el usuario esté activo para recibir OTP.
     *
     * @param usuario Usuario a validar
     * @param cedula  Cédula del usuario (para logging)
     * @throws OtpBusinessException si el usuario está inactivo
     */
    public void validarUsuarioActivo(Usuario usuario, String cedula) {
        if (OtpConstants.ESTADO_INACTIVO.equals(usuario.getEstadoUsuario())) {
            log.warn(OtpConstants.LOG_SOLICITUD_OTP_USUARIO_INACTIVO, cedula);
            throw new OtpBusinessException(
                    OtpConstants.ERROR_USUARIO_INACTIVO,
                    OtpConstants.CODE_USER_INACTIVE);
        }
    }

    /**
     * Valida que el usuario tenga un teléfono registrado.
     *
     * @param telefono Número de teléfono a validar
     * @param cedula   Cédula del usuario (para logging)
     * @throws OtpBusinessException si el teléfono es nulo o vacío
     */
    public void validarTelefonoRegistrado(String telefono, String cedula) {
        if (telefono == null || telefono.trim().isEmpty()) {
            log.error(OtpConstants.LOG_USUARIO_SIN_TELEFONO, cedula);
            throw new OtpBusinessException(
                    OtpConstants.ERROR_TELEFONO_NO_REGISTRADO,
                    OtpConstants.CODE_PHONE_NOT_REGISTERED);
        }
    }
}
