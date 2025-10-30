package com.saferoute.constants;

/**
 * Constantes para el servicio de historial de pedidos.
 */
public final class HistorialConstants {

    private HistorialConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes");
    }

    // ========== MENSAJES DE LOG ==========
    public static final String LOG_CONSULTANDO_HISTORICO = " Consultando histórico - Cliente: {}, Producto: {}, FechaInicio: {}, FechaFin: {}, Estado: {}";
    public static final String LOG_HISTORICO_OBTENIDO = " Histórico obtenido - {} registros encontrados";
    public static final String LOG_OBTENIENDO_DETALLE_PEDIDO = " Obteniendo detalle de pedido ID: {}";
    public static final String LOG_DETALLE_PEDIDO_OBTENIDO = " Detalle de pedido obtenido - {} solicitudes";

    // ========== MENSAJES DE ERROR ==========
    public static final String ERROR_PEDIDO_NO_ENCONTRADO = "Pedido no encontrado";
    public static final String ERROR_PEDIDO_NO_ENCONTRADO_CON_ID = "Pedido no encontrado con ID: %d";
}
