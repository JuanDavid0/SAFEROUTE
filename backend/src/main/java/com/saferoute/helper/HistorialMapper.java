package com.saferoute.helper;

import com.saferoute.dto.HistorialPedidoDTO;
import com.saferoute.dto.SolicitudDTO;
import com.saferoute.dto.SolicitudProductoDTO;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.SolicitudProducto;
import com.saferoute.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper para mapeo de entidades de historial a DTOs.
 */
@Component
@RequiredArgsConstructor
public class HistorialMapper {

    private final SolicitudRepository solicitudRepository;

    /**
     * Convierte un Pedido completo a HistorialPedidoDTO con todas sus solicitudes.
     *
     * @param pedido Entidad Pedido
     * @return DTO con información completa del historial
     */
    public HistorialPedidoDTO toDTO(Pedido pedido) {
        List<Solicitud> solicitudes = solicitudRepository.findByPedido_IdPedido(pedido.getIdPedido());

        HistorialPedidoDTO dto = new HistorialPedidoDTO();
        dto.setIdPedido(pedido.getIdPedido());
        dto.setIdAdmin(pedido.getAdmin().getIdUsuario());
        dto.setEstadoPedido(pedido.getEstadoPedido().name());
        dto.setFechaCreado(pedido.getFechaCreado());
        dto.setFechaCierre(pedido.getFechaCierre());
        dto.setTotalSolicitudes(solicitudes.size());
        dto.setPrecioTotal(calcularPrecioTotal(solicitudes));
        dto.setSolicitudes(mapearSolicitudes(solicitudes));

        return dto;
    }

    /**
     * Calcula el precio total sumando todos los productos de todas las solicitudes.
     *
     * @param solicitudes Lista de solicitudes del pedido
     * @return Precio total calculado
     */
    private BigDecimal calcularPrecioTotal(List<Solicitud> solicitudes) {
        return solicitudes.stream()
                .flatMap(s -> s.getProductos().stream())
                .map(this::calcularSubtotalProducto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el subtotal de un producto (precio * cantidad).
     *
     * @param producto SolicitudProducto
     * @return Subtotal del producto
     */
    private BigDecimal calcularSubtotalProducto(SolicitudProducto producto) {
        return producto.getPrecio().multiply(BigDecimal.valueOf(producto.getCantidadSolicitada()));
    }

    /**
     * Mapea lista de solicitudes a DTOs.
     *
     * @param solicitudes Lista de entidades Solicitud
     * @return Lista de SolicitudDTO
     */
    private List<SolicitudDTO> mapearSolicitudes(List<Solicitud> solicitudes) {
        return solicitudes.stream()
                .map(this::mapearSolicitud)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una Solicitud a SolicitudDTO incluyendo nombre del cliente.
     *
     * @param solicitud Entidad Solicitud
     * @return DTO con información de la solicitud
     */
    private SolicitudDTO mapearSolicitud(Solicitud solicitud) {
        SolicitudDTO dto = new SolicitudDTO();
        dto.setIdSolicitud(solicitud.getIdSolicitud());
        dto.setIdCliente(solicitud.getCliente().getIdUsuario());
        dto.setNombreCliente(construirNombreCompleto(solicitud));
        dto.setIdPedido(solicitud.getPedido().getIdPedido());
        dto.setEstadoSolicitud(solicitud.getEstadoSolicitud().name());
        dto.setDireccionEntrega(solicitud.getDireccionEntrega());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setModificacionesRestantes(solicitud.getModificacionesRestantes());
        dto.setProductos(mapearProductos(solicitud.getProductos()));

        return dto;
    }

    /**
     * Construye el nombre completo del cliente.
     *
     * @param solicitud Solicitud con información del cliente
     * @return Nombre completo formateado
     */
    private String construirNombreCompleto(Solicitud solicitud) {
        return solicitud.getCliente().getNombres() + " " +
                solicitud.getCliente().getApellidos();
    }

    /**
     * Mapea lista de productos de solicitud a DTOs.
     *
     * @param productos Set de SolicitudProducto
     * @return Lista de SolicitudProductoDTO
     */
    private List<SolicitudProductoDTO> mapearProductos(Set<SolicitudProducto> productos) {
        return productos.stream()
                .map(this::mapearProducto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un SolicitudProducto a DTO.
     *
     * @param producto Entidad SolicitudProducto
     * @return DTO con información del producto
     */
    private SolicitudProductoDTO mapearProducto(SolicitudProducto producto) {
        SolicitudProductoDTO dto = new SolicitudProductoDTO();
        dto.setIdProducto(producto.getProducto().getIdProducto());
        dto.setNombreProducto(producto.getProducto().getNombreProducto());
        dto.setCantidadSolicitada(producto.getCantidadSolicitada());
        dto.setPrecio(producto.getPrecio());
        return dto;
    }
}
