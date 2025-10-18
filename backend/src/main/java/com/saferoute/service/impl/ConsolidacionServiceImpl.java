package com.saferoute.service.impl;

import com.saferoute.dto.ConsolidacionDTO;
import com.saferoute.dto.ConsolidacionProductoDTO;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.SolicitudProducto;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.PedidoRepository;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.service.interfaces.IConsolidacionService;
import com.saferoute.service.interfaces.ILogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConsolidacionServiceImpl implements IConsolidacionService {

    private final PedidoRepository pedidoRepository;
    private final SolicitudRepository solicitudRepository;
    private final ILogService logService;

    public ConsolidacionServiceImpl(PedidoRepository pedidoRepository,
            SolicitudRepository solicitudRepository,
            ILogService logService) {
        this.pedidoRepository = pedidoRepository;
        this.solicitudRepository = solicitudRepository;
        this.logService = logService;
    }

    @Override
    public ConsolidacionDTO consolidarPedido(Integer idPedido, Map<String, Object> opciones) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!pedido.getEstadoPedido().equals(EstadoPedidoEnum.ACT)) {
            throw new RuntimeException("Solo se pueden consolidar pedidos activos");
        }

        List<Solicitud> solicitudesPagadas = solicitudRepository
                .findByPedido_IdPedidoAndEstadoSolicitud(idPedido, EstadoSolicitudEnum.PGD);

        if (solicitudesPagadas.isEmpty()) {
            throw new RuntimeException("No hay solicitudes pagadas para consolidar");
        }

        // Cambiar estado del pedido a RTA (En Ruta)
        String estadoAnterior = pedido.getEstadoPedido().name();
        pedido.setEstadoPedido(EstadoPedidoEnum.RTA);
        pedidoRepository.save(pedido);

        // Registrar en log
        logService.registrarCambioEstadoPedido(
                pedido.getAdmin().getIdUsuario(),
                idPedido,
                estadoAnterior,
                EstadoPedidoEnum.RTA.name());

        // Calcular totales
        BigDecimal montoTotal = solicitudesPagadas.stream()
                .flatMap(s -> s.getProductos().stream())
                .map(sp -> sp.getPrecio().multiply(BigDecimal.valueOf(sp.getCantidadSolicitada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Consolidar productos
        Map<Integer, Integer> productosConsolidados = solicitudesPagadas.stream()
                .flatMap(s -> s.getProductos().stream())
                .collect(Collectors.groupingBy(
                        sp -> sp.getProducto().getIdProducto(),
                        Collectors.summingInt(SolicitudProducto::getCantidadSolicitada)));

        List<ConsolidacionProductoDTO> productos = productosConsolidados.entrySet().stream()
                .map(entry -> {
                    ConsolidacionProductoDTO dto = new ConsolidacionProductoDTO();
                    dto.setIdProducto(entry.getKey());
                    dto.setCantidadTotal(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());

        ConsolidacionDTO resultado = new ConsolidacionDTO();
        resultado.setIdPedido(idPedido);
        resultado.setNuevoEstado(EstadoPedidoEnum.RTA.name());
        resultado.setTotalSolicitudes(solicitudesPagadas.size());
        resultado.setMontoTotal(montoTotal);
        resultado.setProductos(productos);
        resultado.setMensaje("Pedido consolidado exitosamente");

        return resultado;
    }
}