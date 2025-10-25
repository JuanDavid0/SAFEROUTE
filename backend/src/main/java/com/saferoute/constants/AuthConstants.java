package com.saferoute.constants;

/**
 * Constantes para el servicio de autenticación.
 */
public final class AuthConstants {

    private AuthConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes");
    }

    // ========== MENSAJES DE LOG ==========
    public static final String LOG_LOGIN_EXITOSO = "🔐 Inicio de sesión exitoso - Rol: {}";
    public static final String LOG_REGISTRO_USUARIO = "👤 Registro de nuevo usuario - Cédula: {}, Rol: {}";
    public static final String LOG_CAMBIO_CONTRASENIA = "🔑 Cambio de contraseña exitoso - Cédula: {}";
    public static final String LOG_CREACION_ADMINISTRADOR = "👨‍💼 Creación de nuevo administrador - Cédula: {}, Nombres: {} {}";

    // ========== MENSAJES DE ERROR ==========
    public static final String ERROR_CEDULA_YA_REGISTRADA = "La cédula ya está registrada";
    public static final String ERROR_ROL_NO_ENCONTRADO = "Rol %s no encontrado";
    public static final String ERROR_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String ERROR_CONTRASENIA_INCORRECTA = "La contraseña actual es incorrecta";
    public static final String ERROR_USUARIO_SAD_NO_ENCONTRADO = "Usuario SAD no encontrado";

    // ========== TIPOS DE ROL ==========
    public static final String ROL_CLIENTE = "CLI";
    public static final String ROL_ADMINISTRADOR = "ADM";
    public static final String ROL_SUPER_ADMIN = "SAD";
    public static final String PREFIJO_ROL_SPRING = "ROLE_";

    // ========== VALORES POR DEFECTO ==========
    public static final String CONTRASENIA_VACIA = "";
}
