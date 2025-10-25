package com.saferoute.constants;

/**
 * Constantes para el servicio de autenticación OTP.
 */
public final class OtpConstants {

    private OtpConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes");
    }

    // ========== MENSAJES DE LOG ==========
    public static final String LOG_SOLICITUD_OTP_CEDULA_NO_REGISTRADA = "⚠️ Intento de solicitar OTP para cédula no registrada: {}";
    public static final String LOG_SOLICITUD_OTP_USUARIO_INACTIVO = "⚠️ Intento de solicitar OTP para usuario inactivo: {}";
    public static final String LOG_USUARIO_SIN_TELEFONO = "❌ Usuario {} no tiene número de teléfono registrado";
    public static final String LOG_ERROR_ENVIAR_SMS = "❌ Error al enviar SMS OTP para cédula: {}";
    public static final String LOG_OTP_GENERADO_EXITOSAMENTE = "✅ OTP generado y enviado exitosamente para cédula: {}";
    public static final String LOG_TOKEN_OTP_NO_ENCONTRADO = "⚠️ No se encontró un token OTP válido para cédula: {}";
    public static final String LOG_TOKEN_OTP_EXPIRADO = "⚠️ Token OTP expirado para cédula: {}";
    public static final String LOG_MAX_INTENTOS_ALCANZADO = "⚠️ Máximo de intentos alcanzado para OTP de cédula: {}";
    public static final String LOG_CODIGO_OTP_INCORRECTO = "⚠️ Código OTP incorrecto para cédula: {}. Intentos restantes: {}";
    public static final String LOG_OTP_VERIFICADO_EXITOSAMENTE = "✅ OTP verificado exitosamente para cédula: {}";
    public static final String LOG_TOKENS_EXPIRADOS_ELIMINADOS = "🧹 Tokens OTP expirados eliminados exitosamente";
    public static final String LOG_ERROR_LIMPIAR_TOKENS = "❌ Error al limpiar tokens OTP expirados: {}";

    // ========== MENSAJES DE ERROR ==========
    public static final String ERROR_USUARIO_NO_ENCONTRADO = "Usuario no encontrado con cédula: %s";
    public static final String ERROR_USUARIO_INACTIVO = "El usuario está inactivo y no puede recibir códigos OTP";
    public static final String ERROR_TELEFONO_NO_REGISTRADO = "El usuario no tiene un número de teléfono registrado. Contacta al administrador.";
    public static final String ERROR_SMS_ENVIO_FALLIDO = "Error al enviar el código de verificación por SMS. Por favor, inténtalo de nuevo.";
    public static final String ERROR_OTP_NO_ENCONTRADO = "No hay un código de verificación válido. Solicita uno nuevo.";
    public static final String ERROR_OTP_EXPIRADO = "El código de verificación ha expirado. Solicita uno nuevo.";
    public static final String ERROR_OTP_MAX_INTENTOS = "Has excedido el número máximo de intentos. Solicita un nuevo código.";
    public static final String ERROR_OTP_CODIGO_INCORRECTO = "Código incorrecto. Te quedan %d intento(s).";

    // ========== CÓDIGOS DE ERROR ==========
    public static final String CODE_USER_INACTIVE = "USER_INACTIVE";
    public static final String CODE_PHONE_NOT_REGISTERED = "PHONE_NOT_REGISTERED";
    public static final String CODE_SMS_SEND_FAILED = "SMS_SEND_FAILED";
    public static final String CODE_OTP_NOT_FOUND = "OTP_NOT_FOUND";
    public static final String CODE_OTP_EXPIRED = "OTP_EXPIRED";
    public static final String CODE_OTP_MAX_ATTEMPTS = "OTP_MAX_ATTEMPTS_REACHED";
    public static final String CODE_OTP_INVALID = "OTP_INVALID";

    // ========== MENSAJES DE RESPUESTA ==========
    public static final String MENSAJE_OTP_ENVIADO = "Código de verificación enviado al número terminado en %s";
    public static final String MENSAJE_VERIFICACION_EXITOSA = "Verificación exitosa";

    // ========== ESTADOS DE USUARIO ==========
    public static final String ESTADO_INACTIVO = "INACTIVO";

    // ========== CONFIGURACIÓN ==========
    public static final int LONGITUD_ULTIMOS_DIGITOS_TELEFONO = 4;
}
