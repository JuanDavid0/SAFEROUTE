package com.saferoute.service.interfaces;

import com.saferoute.dto.PedidoDTO;
import java.util.List;

public interface IPedidoService {
    PedidoDTO crearPedido(PedidoDTO pedidoDTO, Integer idAdmin);
    PedidoDTO actualizarEstado(Integer idPedido, String nuevoEstado);
    PedidoDTO actualizarPedido(Integer idPedido, PedidoDTO pedidoDTO);
    void cancelarPedido(Integer idPedido);
    List<PedidoDTO> listarPedidos();
    PedidoDTO obtenerPedidoPorId(Integer id);
}
