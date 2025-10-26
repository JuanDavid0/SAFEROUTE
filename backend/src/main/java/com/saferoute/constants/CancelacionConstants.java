package com.saferoute.constants;

/**
 * Constantes para el módulo de cancelación de solicitudes
 */
public final class CancelacionConstants {

    private CancelacionConstants() {
        throw new UnsupportedOperationException("Clase de constantes no instanciable");
    }

    // ==================== MENSAJES DE ÉXITO ====================

    public static final String MSG_CANCELACION_EXITOSA = "Canceladas %d de %d solicitudes pendientes.";
    public static final String MSG_CANCELACION_MASIVA_EXITOSA = "Procesados %d pedidos vencidos. Canceladas: %d solicitudes.";
    public static final String MSG_SIN_SOLICITUDES_PENDIENTES = "No hay solicitudes pendientes de pago para cancelar.";
    public static final String MSG_SIN_PEDIDOS_VENCIDOS = "No hay pedidos vencidos con solicitudes pendientes.";

    // ==================== MENSAJES DE ERROR ====================

    public static final String ERROR_PEDIDO_NO_ENCONTRADO = "Pedido no encontrado: %d";
    public static final String ERROR_FECHA_NO_VENCIDA = "La fecha de cierre del pedido aún no ha pasado. No se puede cancelar.";
    public static final String ERROR_PEDIDO_NO_ACTIVO = "El pedido no está activo. Estado actual: %s";
    public static final String ERROR_CANCELACION_SOLICITUD = "Error cancelando solicitud %d: %s";
    public static final String ERROR_PROCESANDO_PEDIDO = "Pedido %d: Error - %s";

    // ==================== LOGS ====================

    public static final String LOG_INICIO_CANCELACION_INDIVIDUAL = "Iniciando cancelación de solicitudes pendientes para pedido: {}";
    public static final String LOG_INICIO_CANCELACION_MASIVA = "Iniciando cancelación masiva de solicitudes vencidas";
    public static final String LOG_SOLICITUD_CANCELADA = "Solicitud {} cancelada exitosamente";
    public static final String LOG_NOTIFICACION_ENVIADA = "Notificación WhatsApp enviada para solicitud: {}";
    public static final String LOG_ERROR_NOTIFICACION = "No se pudo enviar notificación WhatsApp para solicitud {}: {}";
    public static final String LOG_ERROR_CANCELACION = "Error cancelando solicitud {}: {}";
    public static final String LOG_ERROR_PROCESANDO_PEDIDO = "Error procesando pedido {}: {}";
}
