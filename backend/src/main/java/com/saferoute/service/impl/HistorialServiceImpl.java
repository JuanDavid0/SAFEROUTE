package com.saferoute.service.impl;

import com.saferoute.dto.HistorialPedidoDTO;
import com.saferoute.dto.SolicitudDTO;
import com.saferoute.dto.SolicitudProductoDTO;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.SolicitudProducto;
import com.saferoute.repository.PedidoRepository;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.service.interfaces.IHistorialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HistorialServiceImpl implements IHistorialService {

    private final PedidoRepository pedidoRepository;
    private final SolicitudRepository solicitudRepository;

    public HistorialServiceImpl(PedidoRepository pedidoRepository, SolicitudRepository solicitudRepository) {
        this.pedidoRepository = pedidoRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    public List<HistorialPedidoDTO> consultarHistorico(Integer idCliente, Integer idProducto,
            LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        List<Pedido> pedidos = pedidoRepository.findAll();

        // Aplicar filtros
        if (fechaInicio != null) {
            pedidos = pedidos.stream()
                    .filter(p -> !p.getFechaCreado().isBefore(fechaInicio))
                    .collect(Collectors.toList());
        }
        if (fechaFin != null) {
            pedidos = pedidos.stream()
                    .filter(p -> !p.getFechaCreado().isAfter(fechaFin))
                    .collect(Collectors.toList());
        }
        if (estado != null) {
            pedidos = pedidos.stream()
                    .filter(p -> p.getEstadoPedido().name().equals(estado))
                    .collect(Collectors.toList());
        }

        return pedidos.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public HistorialPedidoDTO obtenerDetallePedido(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        return mapToDTO(pedido);
    }

    private HistorialPedidoDTO mapToDTO(Pedido pedido) {
        HistorialPedidoDTO dto = new HistorialPedidoDTO();
        dto.setIdPedido(pedido.getIdPedido());
        dto.setIdAdmin(pedido.getAdmin().getIdUsuario());
        dto.setEstadoPedido(pedido.getEstadoPedido().name());
        dto.setFechaCreado(pedido.getFechaCreado());
        dto.setFechaCierre(pedido.getFechaCierre());

        // Obtener todas las solicitudes del pedido
        List<Solicitud> solicitudes = solicitudRepository.findByPedido_IdPedido(pedido.getIdPedido());
        dto.setTotalSolicitudes(solicitudes.size());

        // Calcular precio total del pedido (suma de todos los productos de todas las
        // solicitudes)
        BigDecimal precioTotal = solicitudes.stream()
                .flatMap(s -> s.getProductos().stream())
                .map(sp -> sp.getPrecio().multiply(BigDecimal.valueOf(sp.getCantidadSolicitada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setPrecioTotal(precioTotal);

        // Mapear solicitudes con nombres de clientes
        List<SolicitudDTO> solicitudesDTO = solicitudes.stream()
                .map(this::mapSolicitudToDTO)
                .collect(Collectors.toList());
        dto.setSolicitudes(solicitudesDTO);

        return dto;
    }

    /**
     * Mapea una Solicitud a SolicitudDTO incluyendo el nombre completo del cliente
     */
    private SolicitudDTO mapSolicitudToDTO(Solicitud solicitud) {
        SolicitudDTO dto = new SolicitudDTO();
        dto.setIdSolicitud(solicitud.getIdSolicitud());
        dto.setIdCliente(solicitud.getCliente().getIdUsuario());

        // Agregar nombre completo del cliente
        String nombreCompleto = solicitud.getCliente().getNombres() + " " +
                solicitud.getCliente().getApellidos();
        dto.setNombreCliente(nombreCompleto);

        dto.setIdPedido(solicitud.getPedido().getIdPedido());
        dto.setEstadoSolicitud(solicitud.getEstadoSolicitud().name());
        dto.setDireccionEntrega(solicitud.getDireccionEntrega());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        // Incluir modificaciones restantes a nivel de solicitud
        dto.setModificacionesRestantes(solicitud.getModificacionesRestantes());

        // Mapear productos de la solicitud
        List<SolicitudProductoDTO> productosDTO = solicitud.getProductos().stream()
                .map(sp -> {
                    SolicitudProductoDTO pDTO = new SolicitudProductoDTO();
                    pDTO.setIdProducto(sp.getProducto().getIdProducto());
                    pDTO.setNombreProducto(sp.getProducto().getNombreProducto()); // Incluir nombre del producto
                    pDTO.setCantidadSolicitada(sp.getCantidadSolicitada());
                    pDTO.setPrecio(sp.getPrecio());
                    return pDTO;
                })
                .collect(Collectors.toList());
        dto.setProductos(productosDTO);

        return dto;
    }
}
