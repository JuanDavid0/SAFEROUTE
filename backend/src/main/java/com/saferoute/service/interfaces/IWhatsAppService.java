package com.saferoute.service.interfaces;

import com.saferoute.dto.SolicitudDTO;
import com.saferoute.model.Pedido;

public interface IWhatsAppService {

    void enviarResumenSolicitud(SolicitudDTO solicitud);

    void notificarNuevoPedidoActivo(Pedido pedido);

    void notificarCambioEstadoPedido(Pedido pedido, String estadoAnterior);
    
    void notificarCancelacionPedido(Pedido pedido);

    void notificarSolicitudPagada(SolicitudDTO solicitud);
}
