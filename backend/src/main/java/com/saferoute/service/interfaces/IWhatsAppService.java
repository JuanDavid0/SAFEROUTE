package com.saferoute.service.interfaces;

import com.saferoute.dto.SolicitudDTO;
import com.saferoute.model.Pedido;

public interface IWhatsAppService {

    void enviarResumenSolicitud(SolicitudDTO solicitud);

    void notificarSolicitudPagada(SolicitudDTO solicitud);

    void notificarActualizacionSolicitud(Integer idSolicitud, Integer idPedido,
            String estadoAnterior, String estadoActual, String mensaje);

    void notificarNuevoPedido(Pedido pedido);

    void notificarCancelacionPedido(Pedido pedido, String motivo);

    void notificarCancelacionSolicitud(Integer idSolicitud, Integer idPedido);
}
