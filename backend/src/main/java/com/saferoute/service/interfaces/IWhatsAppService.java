package com.saferoute.service.interfaces;

import com.saferoute.dto.SolicitudDTO;
import com.saferoute.model.Pedido;

/**
 * Servicio para enviar notificaciones por WhatsApp
 */
public interface IWhatsAppService {

    /**
     * Envía resumen de solicitud creada al cliente
     */
    void enviarResumenSolicitud(SolicitudDTO solicitud);

    /**
     * Notifica a todos los clientes sobre un nuevo pedido activo
     */
    void notificarNuevoPedidoActivo(Pedido pedido);

    /**
     * Notifica a los clientes sobre cambio de estado del pedido
     */
    void notificarCambioEstadoPedido(Pedido pedido, String estadoAnterior);

    /**
     * Notifica cancelación de pedido a los clientes afectados
     */
    void notificarCancelacionPedido(Pedido pedido);

    /**
     * Notifica al cliente que su solicitud fue marcada como pagada
     */
    void notificarSolicitudPagada(SolicitudDTO solicitud);
}
