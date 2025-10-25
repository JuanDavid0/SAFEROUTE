package com.saferoute.constants;

/**
 * Constantes para la gestión de usuarios del sistema.
 * Centraliza todos los mensajes, roles y valores hardcoded.
 */
public final class UsuarioConstants {

    private UsuarioConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe ser instanciada");
    }

    // ===== ROLES =====
    public static final String ROL_ADMINISTRADOR = "ADM";

    // ===== ESTADOS =====
    public static final String ESTADO_INACTIVO = "INACTIVO";

    // ===== MENSAJES DE ERROR =====
    public static final String ERROR_USUARIO_NO_ENCONTRADO = "Usuario no encontrado con ID: %d";
    public static final String ERROR_SOLO_ADMINISTRADORES = "Solo se pueden eliminar usuarios con rol de Administrador (ADM)";

    // ===== FORMATOS DE MENSAJES =====
    public static final String FORMATO_LOG_ELIMINACION = "Usuario administrador eliminado (soft delete) - Cédula: %s, Nombres: %s %s";

    // ===== MENSAJES DE LOG =====
    public static final String LOG_ELIMINANDO_USUARIO = "🗑️ Eliminando usuario ID: {}";
    public static final String LOG_VALIDANDO_ROL = "🔍 Validando rol de administrador para usuario ID: {}";
    public static final String LOG_USUARIO_ELIMINADO = "✅ Usuario eliminado exitosamente (soft delete) - ID: {}";
    public static final String LOG_OBTENER_POR_ID = "👤 Obteniendo usuario por ID: {}";
    public static final String LOG_LISTAR_TODOS = "📋 Listando todos los usuarios";
    public static final String LOG_TOTAL_ENCONTRADOS = "🔍 Total de usuarios encontrados: {}";
}
