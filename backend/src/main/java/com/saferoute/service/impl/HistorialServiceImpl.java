package com.saferoute.service.impl;

import com.saferoute.constants.HistorialConstants;
import com.saferoute.dto.HistorialPedidoDTO;
import com.saferoute.exception.HistorialBusinessException;
import com.saferoute.helper.HistorialMapper;
import com.saferoute.model.Pedido;
import com.saferoute.repository.PedidoRepository;
import com.saferoute.service.interfaces.IHistorialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de consulta de historial de pedidos.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HistorialServiceImpl implements IHistorialService {

    private final PedidoRepository pedidoRepository;
    private final HistorialMapper historialMapper;

    @Override
    public List<HistorialPedidoDTO> consultarHistorico(Integer idCliente, Integer idProducto,
            LocalDate fechaInicio, LocalDate fechaFin, String estado) {

        log.info(HistorialConstants.LOG_CONSULTANDO_HISTORICO,
                idCliente, idProducto, fechaInicio, fechaFin, estado);

        List<Pedido> pedidos = pedidoRepository.findAll();
        List<HistorialPedidoDTO> resultado = aplicarFiltrosYMapear(pedidos, fechaInicio, fechaFin, estado);

        log.info(HistorialConstants.LOG_HISTORICO_OBTENIDO, resultado.size());
        return resultado;
    }

    /**
     * Aplica filtros a la lista de pedidos y convierte a DTOs.
     */
    private List<HistorialPedidoDTO> aplicarFiltrosYMapear(List<Pedido> pedidos,
            LocalDate fechaInicio, LocalDate fechaFin, String estado) {

        return pedidos.stream()
                .filter(p -> cumpleFiltroFechaInicio(p, fechaInicio))
                .filter(p -> cumpleFiltroFechaFin(p, fechaFin))
                .filter(p -> cumpleFiltroEstado(p, estado))
                .map(historialMapper::toDTO)
                .collect(Collectors.toList());
    }

    private boolean cumpleFiltroFechaInicio(Pedido pedido, LocalDate fechaInicio) {
        return fechaInicio == null || !pedido.getFechaCreado().isBefore(fechaInicio);
    }

    private boolean cumpleFiltroFechaFin(Pedido pedido, LocalDate fechaFin) {
        return fechaFin == null || !pedido.getFechaCreado().isAfter(fechaFin);
    }

    private boolean cumpleFiltroEstado(Pedido pedido, String estado) {
        return estado == null || pedido.getEstadoPedido().name().equals(estado);
    }

    @Override
    public HistorialPedidoDTO obtenerDetallePedido(Integer idPedido) {
        log.info(HistorialConstants.LOG_OBTENIENDO_DETALLE_PEDIDO, idPedido);

        Pedido pedido = buscarPedidoPorId(idPedido);
        HistorialPedidoDTO detalle = historialMapper.toDTO(pedido);

        log.info(HistorialConstants.LOG_DETALLE_PEDIDO_OBTENIDO, detalle.getTotalSolicitudes());
        return detalle;
    }

    /**
     * Busca un pedido por ID o lanza excepción si no existe.
     */
    private Pedido buscarPedidoPorId(Integer idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new HistorialBusinessException(
                        String.format(HistorialConstants.ERROR_PEDIDO_NO_ENCONTRADO_CON_ID, idPedido)));
    }
}
