package com.saferoute.constants;

/**
 * Constantes para el servicio de consolidación de pedidos.
 */
public final class ConsolidacionConstants {

    private ConsolidacionConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes");
    }

    // ========== MENSAJES DE LOG ==========
    public static final String LOG_CONSOLIDANDO_PEDIDO = "📦 Consolidando pedido ID: {}";
    public static final String LOG_PEDIDO_CONSOLIDADO = "✅ Pedido consolidado exitosamente - ID: {}, Total solicitudes: {}, Monto total: {}";
    public static final String LOG_CAMBIO_ESTADO_PEDIDO = "🔄 Cambio de estado del pedido {} de {} a {}";

    // ========== MENSAJES DE ERROR ==========
    public static final String ERROR_PEDIDO_NO_ENCONTRADO = "Pedido no encontrado";
    public static final String ERROR_PEDIDO_NO_ENCONTRADO_CON_ID = "Pedido no encontrado con ID: %d";
    public static final String ERROR_PEDIDO_NO_ACTIVO = "Solo se pueden consolidar pedidos activos";
    public static final String ERROR_SIN_SOLICITUDES_PAGADAS = "No hay solicitudes pagadas para consolidar";

    // ========== MENSAJES DE RESPUESTA ==========
    public static final String MENSAJE_PEDIDO_CONSOLIDADO = "Pedido consolidado exitosamente";
}
