package com.saferoute.service.impl;

import com.saferoute.config.OtpConfig;
import com.saferoute.constants.OtpConstants;
import com.saferoute.dto.OtpResponse;
import com.saferoute.exception.OtpBusinessException;
import com.saferoute.exception.ResourceNotFoundException;
import com.saferoute.helper.OtpMessageFormatter;
import com.saferoute.helper.OtpTokenValidator;
import com.saferoute.model.OtpToken;
import com.saferoute.model.Usuario;
import com.saferoute.repository.OtpTokenRepository;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.security.JwtTokenProvider;
import com.saferoute.service.interfaces.IOtpService;
import com.saferoute.validator.OtpValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Implementación del servicio de autenticación OTP.
 * Gestiona el ciclo completo: generación, envío, verificación y limpieza de
 * tokens OTP.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OtpServiceImpl implements IOtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final TwilioService twilioService;
    private final OtpConfig otpConfig;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpValidator otpValidator;
    private final OtpTokenValidator tokenValidator;
    private final OtpMessageFormatter messageFormatter;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public OtpResponse solicitarOtp(String cedula) {
        log.debug("Iniciando solicitud de OTP para cédula: {}", cedula);

        Usuario usuario = buscarUsuarioPorCedula(cedula);
        otpValidator.validarUsuarioActivo(usuario, cedula);

        String telefono = usuario.getTelefono();
        otpValidator.validarTelefonoRegistrado(telefono, cedula);

        String codigoOtp = generarCodigoOtp();
        crearYGuardarToken(cedula, telefono, codigoOtp);

        enviarSmsOtp(telefono, codigoOtp, cedula);

        log.info(OtpConstants.LOG_OTP_GENERADO_EXITOSAMENTE, cedula);
        return crearRespuestaOtpEnviado(telefono);
    }

    private Usuario buscarUsuarioPorCedula(String cedula) {
        return usuarioRepository.findByCedula(cedula)
                .orElseThrow(() -> {
                    log.warn(OtpConstants.LOG_SOLICITUD_OTP_CEDULA_NO_REGISTRADA, cedula);
                    return new ResourceNotFoundException("Usuario", "cédula", cedula);
                });
    }

    private OtpToken crearYGuardarToken(String cedula, String telefono, String codigoOtp) {
        LocalDateTime fechaExpiracion = LocalDateTime.now()
                .plusMinutes(otpConfig.getExpirationMinutes());

        OtpToken token = new OtpToken(cedula, telefono, codigoOtp, fechaExpiracion);
        return otpTokenRepository.save(token);
    }

    private void enviarSmsOtp(String telefono, String codigoOtp, String cedula) {
        boolean smsEnviado = twilioService.enviarSmsOtp(telefono, codigoOtp);

        if (!smsEnviado) {
            log.error(OtpConstants.LOG_ERROR_ENVIAR_SMS, cedula);
            throw new OtpBusinessException(
                    OtpConstants.ERROR_SMS_ENVIO_FALLIDO,
                    OtpConstants.CODE_SMS_SEND_FAILED);
        }
    }

    private OtpResponse crearRespuestaOtpEnviado(String telefono) {
        String mensaje = messageFormatter.formatearMensajeOtpEnviado(telefono);
        return new OtpResponse(true, mensaje);
    }

    @Override
    public OtpResponse verificarOtp(String cedula, String codigoOtp) {
        log.debug("Iniciando verificación de OTP para cédula: {}", cedula);

        OtpToken token = buscarTokenValido(cedula);

        tokenValidator.validarTokenNoExpirado(token, cedula);
        tokenValidator.validarMaximosIntentos(token, cedula);

        if (!tokenValidator.validarCodigoOtp(token, codigoOtp, cedula)) {
            manejarCodigoIncorrecto(token);
        }

        marcarTokenComoVerificado(token);
        String jwtToken = generarTokenTemporal(cedula);

        log.info(OtpConstants.LOG_OTP_VERIFICADO_EXITOSAMENTE, cedula);
        return new OtpResponse(true, OtpConstants.MENSAJE_VERIFICACION_EXITOSA, jwtToken);
    }

    private OtpToken buscarTokenValido(String cedula) {
        return otpTokenRepository
                .findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                        cedula, LocalDateTime.now())
                .orElseThrow(() -> {
                    log.warn(OtpConstants.LOG_TOKEN_OTP_NO_ENCONTRADO, cedula);
                    return new OtpBusinessException(
                            OtpConstants.ERROR_OTP_NO_ENCONTRADO,
                            OtpConstants.CODE_OTP_NOT_FOUND);
                });
    }

    private void manejarCodigoIncorrecto(OtpToken token) {
        token.incrementarIntentosFallidos();
        otpTokenRepository.save(token);

        String mensajeError = tokenValidator.obtenerMensajeIntentosRestantes(token.getIntentosFallidos());
        throw new OtpBusinessException(mensajeError, OtpConstants.CODE_OTP_INVALID);
    }

    private void marcarTokenComoVerificado(OtpToken token) {
        token.setVerificado(true);
        token.setUsado(true);
        otpTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void limpiarTokensExpirados() {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            otpTokenRepository.deleteByFechaExpiracionBefore(ahora);
            log.info(OtpConstants.LOG_TOKENS_EXPIRADOS_ELIMINADOS);
        } catch (Exception e) {
            log.error(OtpConstants.LOG_ERROR_LIMPIAR_TOKENS, e.getMessage());
        }
    }

    /**
     * Genera un código OTP aleatorio de n dígitos.
     */
    private String generarCodigoOtp() {
        int length = otpConfig.getLength();
        int bound = (int) Math.pow(10, length);
        int codigo = secureRandom.nextInt(bound);
        return String.format("%0" + length + "d", codigo);
    }

    /**
     * Genera un token JWT temporal para que el cliente pueda modificar su
     * solicitud.
     * El token tiene un rol especial "OTP_VERIFIED" y contiene la cédula del
     * cliente.
     */
    private String generarTokenTemporal(String cedula) {
        return jwtTokenProvider.generateOtpToken(cedula);
    }
}
