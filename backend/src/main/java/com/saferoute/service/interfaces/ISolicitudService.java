package com.saferoute.service.interfaces;

import com.saferoute.dto.SolicitudDTO;
import com.saferoute.dto.SolicitudModificacionDTO;
import com.saferoute.dto.SolicitudProductoDTO;
import java.util.List;

public interface ISolicitudService {
    SolicitudDTO crearSolicitud(SolicitudDTO solicitudDTO, Integer idCliente);

    SolicitudDTO crearSolicitudClienteNuevo(com.saferoute.dto.SolicitudClienteDTO dto);

    SolicitudDTO modificarSolicitud(Integer idSolicitud, SolicitudModificacionDTO datos);

    SolicitudDTO agregarProducto(Integer idSolicitud, SolicitudProductoDTO productoDTO);

    SolicitudDTO eliminarProducto(Integer idSolicitud, Integer idProducto);

    SolicitudDTO modificarCantidadProducto(Integer idSolicitud, Integer idProducto, Integer nuevaCantidad);

    void cancelarSolicitud(Integer idSolicitud);

    void cancelarSolicitudesVencidas();

    List<SolicitudDTO> listarSolicitudesCliente(Integer idCliente);

    void cambiarEstado(Integer idSolicitud, String nuevoEstado);

    List<SolicitudDTO> listarPorPedido(Integer idPedido);

    List<SolicitudDTO> listarPorPedidoYEstado(Integer idPedido, String estado);
}
