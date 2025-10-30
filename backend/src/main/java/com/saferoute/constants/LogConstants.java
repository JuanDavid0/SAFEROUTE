package com.saferoute.constants;

/**
 * Constantes para la gestión de logs del sistema.
 * Centraliza todos los mensajes, formatos y valores hardcoded.
 */
public final class LogConstants {

    private LogConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe ser instanciada");
    }

    // ===== MENSAJES DE ERROR =====
    public static final String ERROR_USUARIO_NO_ENCONTRADO = "Usuario no encontrado con ID: %d";

    // ===== FORMATOS DE MENSAJES =====
    public static final String FORMATO_CAMBIO_ESTADO = "Cambio de estado del pedido #%d: %s -> %s";

    // ===== MENSAJES DE LOG =====
    public static final String LOG_REGISTRANDO = "Registrando log para usuario ID: {} - Acción: {}";
    public static final String LOG_REGISTRADO = " Log registrado exitosamente - ID: {}";
    public static final String LOG_CAMBIO_ESTADO = "Registrando cambio de estado de pedido - Usuario: {}, Pedido: {}, Estado: {} -> {}";
    public static final String LOG_OBTENER_TODOS = "Obteniendo todos los logs del sistema";
    public static final String LOG_OBTENER_POR_USUARIO = "Obteniendo logs del usuario ID: {}";
    public static final String LOG_OBTENER_POR_FECHA = " Obteniendo logs entre {} y {}";
    public static final String LOG_TOTAL_ENCONTRADOS = "Total de logs encontrados: {}";
}
