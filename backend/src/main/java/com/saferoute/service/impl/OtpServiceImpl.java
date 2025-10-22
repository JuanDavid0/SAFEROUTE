package com.saferoute.service.impl;

import com.saferoute.config.OtpConfig;
import com.saferoute.dto.OtpResponse;
import com.saferoute.exception.BusinessException;
import com.saferoute.exception.ResourceNotFoundException;
import com.saferoute.model.OtpToken;
import com.saferoute.model.Usuario;
import com.saferoute.repository.OtpTokenRepository;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.security.JwtTokenProvider;
import com.saferoute.service.interfaces.IOtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Implementación del servicio de autenticación OTP
 * Guarda tokens en BD, envía SMS personalizados vía Twilio y controla el flujo
 * completo
 */
@Service
@Transactional
public class OtpServiceImpl implements IOtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);

    private final OtpTokenRepository otpTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final TwilioService twilioService;
    private final OtpConfig otpConfig;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom;

    public OtpServiceImpl(OtpTokenRepository otpTokenRepository,
            UsuarioRepository usuarioRepository,
            TwilioService twilioService,
            OtpConfig otpConfig,
            JwtTokenProvider jwtTokenProvider) {
        this.otpTokenRepository = otpTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.twilioService = twilioService;
        this.otpConfig = otpConfig;
        this.jwtTokenProvider = jwtTokenProvider;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public OtpResponse solicitarOtp(String cedula) {
        // Buscar usuario por cédula
        Usuario usuario = usuarioRepository.findByCedula(cedula)
                .orElseThrow(() -> {
                    logger.warn("Intento de solicitar OTP para cédula no registrada: {}", cedula);
                    return new ResourceNotFoundException("Usuario", "cédula", cedula);
                });

        // Verificar que el usuario esté activo
        if ("INACTIVO".equals(usuario.getEstadoUsuario())) {
            logger.warn("Intento de solicitar OTP para usuario inactivo: {}", cedula);
            throw new BusinessException("El usuario está inactivo y no puede recibir códigos OTP", "USER_INACTIVE");
        }

        // Validar teléfono
        String telefono = usuario.getTelefono();
        if (telefono == null || telefono.trim().isEmpty()) {
            logger.error("Usuario {} no tiene número de teléfono registrado", cedula);
            throw new BusinessException(
                    "El usuario no tiene un número de teléfono registrado. Contacta al administrador.",
                    "PHONE_NOT_REGISTERED");
        }

        // Generar código OTP
        String codigoOtp = generarCodigoOtp();

        // Calcular fecha de expiración
        LocalDateTime fechaExpiracion = LocalDateTime.now()
                .plusMinutes(otpConfig.getExpirationMinutes());

        // Crear y guardar token OTP
        OtpToken otpToken = new OtpToken(cedula, telefono, codigoOtp, fechaExpiracion);
        otpTokenRepository.save(otpToken);

        // Enviar SMS con Twilio
        boolean smsEnviado = twilioService.enviarSmsOtp(telefono, codigoOtp);

        if (!smsEnviado) {
            logger.error("Error al enviar SMS OTP para cédula: {}", cedula);
            throw new BusinessException(
                    "Error al enviar el código de verificación por SMS. Por favor, inténtalo de nuevo.",
                    "SMS_SEND_FAILED");
        }

        logger.info("OTP generado y enviado exitosamente para cédula: {}", cedula);
        return new OtpResponse(true,
                String.format("Código de verificación enviado al número terminado en %s",
                        telefono.substring(telefono.length() - 4)));
    }

    @Override
    public OtpResponse verificarOtp(String cedula, String codigoOtp) {
        // Buscar el token OTP más reciente y válido para la cédula
        OtpToken token = otpTokenRepository
                .findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                        cedula, LocalDateTime.now())
                .orElseThrow(() -> {
                    logger.warn("No se encontró un token OTP válido para cédula: {}", cedula);
                    return new BusinessException(
                            "No hay un código de verificación válido. Solicita uno nuevo.",
                            "OTP_NOT_FOUND");
                });

        // Verificar si ha expirado
        if (token.haExpirado()) {
            logger.warn("Token OTP expirado para cédula: {}", cedula);
            throw new BusinessException(
                    "El código de verificación ha expirado. Solicita uno nuevo.",
                    "OTP_EXPIRED");
        }

        // Verificar número máximo de intentos
        if (token.getIntentosFallidos() >= otpConfig.getMaxAttempts()) {
            logger.warn("Máximo de intentos alcanzado para OTP de cédula: {}", cedula);
            token.setUsado(true);
            otpTokenRepository.save(token);
            throw new BusinessException(
                    "Has excedido el número máximo de intentos. Solicita un nuevo código.",
                    "OTP_MAX_ATTEMPTS_REACHED");
        }

        // Verificar el código OTP
        if (!token.getCodigoOtp().equals(codigoOtp)) {
            token.incrementarIntentosFallidos();
            otpTokenRepository.save(token);

            int intentosRestantes = otpConfig.getMaxAttempts() - token.getIntentosFallidos();
            logger.warn("Código OTP incorrecto para cédula: {}. Intentos restantes: {}",
                    cedula, intentosRestantes);

            throw new BusinessException(
                    String.format("Código incorrecto. Te quedan %d intento(s).", intentosRestantes),
                    "OTP_INVALID");
        }

        // Código correcto - marcar como verificado y usado
        token.setVerificado(true);
        token.setUsado(true);
        otpTokenRepository.save(token);

        // Generar token JWT temporal para permitir modificación de solicitud
        String jwtToken = generarTokenTemporal(cedula);

        logger.info("OTP verificado exitosamente para cédula: {}", cedula);
        return new OtpResponse(true, "Verificación exitosa", jwtToken);
    }

    @Override
    @Transactional
    public void limpiarTokensExpirados() {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            otpTokenRepository.deleteByFechaExpiracionBefore(ahora);
            logger.info("Tokens OTP expirados eliminados exitosamente");
        } catch (Exception e) {
            logger.error("Error al limpiar tokens OTP expirados: {}", e.getMessage());
        }
    }

    /**
     * Genera un código OTP aleatorio de n dígitos
     */
    private String generarCodigoOtp() {
        int length = otpConfig.getLength();
        int bound = (int) Math.pow(10, length);
        int codigo = secureRandom.nextInt(bound);
        return String.format("%0" + length + "d", codigo);
    }

    /**
     * Genera un token JWT temporal para que el cliente pueda modificar su solicitud
     * El token tiene un rol especial "OTP_VERIFIED" y contiene la cédula del
     * cliente
     */
    private String generarTokenTemporal(String cedula) {
        return jwtTokenProvider.generateOtpToken(cedula);
    }
}
