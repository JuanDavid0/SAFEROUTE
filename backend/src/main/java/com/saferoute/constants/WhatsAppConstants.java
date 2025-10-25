package com.saferoute.constants;

/**
 * Constantes para el servicio de WhatsApp.
 */
public final class WhatsAppConstants {

    private WhatsAppConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes");
    }

    // ========== MENSAJES DE LOG ==========
    public static final String LOG_ENVIANDO_RESUMEN_SOLICITUD = " Iniciando envío de resumen de solicitud #{} al cliente";
    public static final String LOG_CLIENTE_INFO = " Cliente: {} - Teléfono: {}";
    public static final String LOG_RESUMEN_ENVIADO = " Resumen de solicitud #{} enviado exitosamente";
    public static final String LOG_ERROR_RESUMEN = " Error al enviar resumen de solicitud #{}: {}";

    public static final String LOG_NOTIFICANDO_PEDIDO_ACTIVO = " Notificando nuevo pedido activo #{} a todos los clientes";
    public static final String LOG_PEDIDO_ACTIVO_ENVIADO = " Notificación de nuevo pedido enviada a {} clientes";
    public static final String LOG_ERROR_PEDIDO_ACTIVO = " Error al notificar nuevo pedido activo: {}";

    public static final String LOG_NOTIFICANDO_CAMBIO_ESTADO = " Notificando cambio de estado del pedido #{}: {} -> {}";
    public static final String LOG_CAMBIO_ESTADO_ENVIADO = " Notificación de cambio de estado enviada a {} clientes";
    public static final String LOG_ERROR_CAMBIO_ESTADO = " Error al notificar cambio de estado: {}";

    public static final String LOG_NOTIFICANDO_CANCELACION = " Notificando cancelación del pedido #{}";
    public static final String LOG_CANCELACION_ENVIADA = " Notificación de cancelación enviada a {} clientes";
    public static final String LOG_ERROR_CANCELACION = " Error al notificar cancelación: {}";

    public static final String LOG_NOTIFICANDO_SOLICITUD_PAGADA = " Notificando solicitud pagada #{} al cliente";
    public static final String LOG_SOLICITUD_PAGADA_ENVIADA = " Notificación de solicitud pagada enviada exitosamente";
    public static final String LOG_ERROR_SOLICITUD_PAGADA = " Error al notificar solicitud pagada: {}";

    public static final String LOG_ENVIANDO_PLANTILLA = " Enviando plantilla '{}' a WhatsApp - Número: {}";
    public static final String LOG_REQUEST_BODY = " Request body: {}";
    public static final String LOG_PLANTILLA_ENVIADA = " Plantilla '{}' enviada exitosamente a {} - Response: {}";
    public static final String LOG_ERROR_PLANTILLA = " Error al enviar plantilla. Status: {}, Body: {}";
    public static final String LOG_ERROR_ENVIO_MENSAJE = "Error al enviar mensaje a {}: {}";
    public static final String LOG_PEDIDO_SIN_HASH = " Pedido #{} no tiene URL hash, usando ID en la URL";
    public static final String LOG_URL_PEDIDO_HASH = "🔗 URL del pedido usando hash: {}";

    // ========== MENSAJES DE ERROR HTTP ==========
    public static final String ERROR_401_TOKEN_INVALIDO = " ERROR 401: Token de WhatsApp inválido o expirado";
    public static final String ERROR_403_NUMERO_NO_AUTORIZADO = " ERROR 403: Número {} no autorizado";
    public static final String ERROR_400_PLANTILLA_NO_ENCONTRADA = " ERROR 400: Plantilla '{}' no encontrada o no aprobada. "
            +
            "Verifica que la plantilla esté creada y aprobada en Meta for Developers";
    public static final String ERROR_HTTP_GENERICO = " Error HTTP {} al enviar plantilla a {}: {}";
    public static final String ERROR_INESPERADO = " Error inesperado al enviar plantilla a {}: {}";

    // ========== MENSAJES DE EXCEPCIÓN ==========
    public static final String EXCEPTION_ERROR_ENVIAR_PLANTILLA = "Error al enviar plantilla de WhatsApp";

    // ========== NOMBRES DE PLANTILLAS ==========
    public static final String TEMPLATE_RESUMEN_SOLICITUD = "resumen_solicitud";
    public static final String TEMPLATE_NUEVO_PEDIDO_ACTIVO = "nuevo_pedido_activo";
    public static final String TEMPLATE_CAMBIO_ESTADO_PEDIDO = "cambio_estado_pedido";
    public static final String TEMPLATE_PEDIDO_CANCELADO = "pedido_cancelado";
    public static final String TEMPLATE_SOLICITUD_PAGADA = "solicitud_pagada";

    // ========== ROLES Y ESTADOS ==========
    public static final String ROL_CLIENTE = "CLI";
    public static final String ESTADO_USUARIO_ACTIVO = "ACTIVO";

    // ========== CONFIGURACIÓN WHATSAPP ==========
    public static final String MESSAGING_PRODUCT = "whatsapp";
    public static final String RECIPIENT_TYPE = "individual";
    public static final String MESSAGE_TYPE = "template";
    public static final String COMPONENT_TYPE_BODY = "body";

    // ========== CÓDIGOS DE PAÍS ==========
    public static final String CODIGO_PAIS_COLOMBIA = "57";
    public static final int LONGITUD_TELEFONO_COLOMBIA = 10;

    // ========== CÓDIGOS HTTP ==========
    public static final int HTTP_STATUS_BAD_REQUEST = 400;
    public static final int HTTP_STATUS_UNAUTHORIZED = 401;
    public static final int HTTP_STATUS_FORBIDDEN = 403;

    // ========== TRADUCCIONES DE ESTADOS ==========
    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_CERRADO = "Cerrado";
    public static final String ESTADO_LISTO_ADUANAS = "Listo para Aduanas";
    public static final String ESTADO_EN_ADUANAS = "En Aduanas";
    public static final String ESTADO_EN_DISTRIBUCION = "En Distribución";
    public static final String ESTADO_ENTREGADO = "Entregado";
    public static final String ESTADO_CANCELADO_MANUAL = "Cancelado (Manual)";
    public static final String ESTADO_CANCELADO_AUTOMATICO = "Cancelado (Automático)";

    // ========== MENSAJES SEGÚN ESTADO ==========
    public static final String MENSAJE_LISTO_ADUANAS = " Tu pedido está listo para aduanas.\n¡Pronto estará en camino!";
    public static final String MENSAJE_EN_ADUANAS = " Tu pedido está en proceso de aduanas.\nEsto puede tomar algunos días.";
    public static final String MENSAJE_EN_DISTRIBUCION = " Tu pedido está en distribución.\n¡Llegará pronto a tu destino!";
    public static final String MENSAJE_ENTREGADO = " ¡Tu pedido ha sido entregado!\nGracias por confiar en nosotros.";
    public static final String MENSAJE_DEFAULT = "Mantente atento a las próximas actualizaciones.";

    // ========== FORMATOS DE URL ==========
    public static final String URL_FORMAT_SOLICITUD = "/solicitud/%d";
    public static final String URL_FORMAT_PEDIDO_HASH = "/pedidos/pedido-disponible/%s";
    public static final String URL_FORMAT_PEDIDO_ID = "/pedidos/pedido/%d";
    public static final String URL_FORMAT_MESSAGES_API = "%s/%s/messages";
}
