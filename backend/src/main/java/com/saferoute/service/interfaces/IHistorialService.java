package com.saferoute.service.interfaces;

import com.saferoute.dto.HistorialPedidoDTO;
import java.time.LocalDate;
import java.util.List;

public interface IHistorialService {
    List<HistorialPedidoDTO> consultarHistorico(Integer idCliente, Integer idProducto, 
                                                LocalDate fechaInicio, LocalDate fechaFin, String estado);
    HistorialPedidoDTO obtenerDetallePedido(Integer idPedido);
}