package com.saferoute.service.interfaces;

import com.saferoute.dto.PedidoDTO;
import com.saferoute.dto.ProductoPedidoDTO;
import java.util.List;

public interface IPedidoService {
    PedidoDTO crearPedido(PedidoDTO pedidoDTO, Integer idAdmin);

    PedidoDTO actualizarEstado(Integer idPedido, String nuevoEstado);

    PedidoDTO actualizarPedido(Integer idPedido, PedidoDTO pedidoDTO);

    void cancelarPedido(Integer idPedido);

    List<PedidoDTO> listarPedidos();

    PedidoDTO obtenerPedidoPorId(Integer id);

    // Gestión de productos del pedido
    PedidoDTO agregarProducto(Integer idPedido, ProductoPedidoDTO productoDTO);

    PedidoDTO eliminarProducto(Integer idPedido, Integer idProducto);

    PedidoDTO modificarProducto(Integer idPedido, Integer idProducto, ProductoPedidoDTO productoDTO);
}
