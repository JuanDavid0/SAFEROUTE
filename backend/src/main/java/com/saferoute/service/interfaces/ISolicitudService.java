package com.saferoute.service.interfaces;

import com.saferoute.dto.SolicitudDTO;
import com.saferoute.dto.SolicitudModificacionDTO;
import java.util.List;

public interface ISolicitudService {
    SolicitudDTO crearSolicitud(SolicitudDTO solicitudDTO, Integer idCliente);

    SolicitudDTO crearSolicitudClienteNuevo(com.saferoute.dto.SolicitudClienteDTO dto);

    SolicitudDTO modificarSolicitud(Integer idSolicitud, SolicitudModificacionDTO datos);

    void cancelarSolicitud(Integer idSolicitud);

    void cancelarSolicitudesVencidas();

    List<SolicitudDTO> listarSolicitudesCliente(Integer idCliente);

    void cambiarEstado(Integer idSolicitud, String nuevoEstado);

    List<SolicitudDTO> listarPorPedido(Integer idPedido);

    List<SolicitudDTO> listarPorPedidoYEstado(Integer idPedido, String estado);
}
