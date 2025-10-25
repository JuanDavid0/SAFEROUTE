package com.saferoute.helper;

import com.saferoute.dto.ConsolidacionProductoDTO;
import com.saferoute.model.Solicitud;
import com.saferoute.model.SolicitudProducto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper para cálculos de consolidación de pedidos.
 */
@Component
public class ConsolidacionCalculosHelper {

    /**
     * Calcula el monto total de todas las solicitudes.
     *
     * @param solicitudes Lista de solicitudes pagadas
     * @return Monto total calculado
     */
    public BigDecimal calcularMontoTotal(List<Solicitud> solicitudes) {
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
     * Consolida productos agrupando por ID y sumando cantidades.
     *
     * @param solicitudes Lista de solicitudes pagadas
     * @return Lista de productos consolidados con cantidades totales
     */
    public List<ConsolidacionProductoDTO> consolidarProductos(List<Solicitud> solicitudes) {
        Map<Integer, Integer> productosAgrupados = agruparProductosPorId(solicitudes);
        return convertirADTOs(productosAgrupados);
    }

    /**
     * Agrupa productos por ID y suma sus cantidades.
     *
     * @param solicitudes Lista de solicitudes
     * @return Mapa de ID producto → cantidad total
     */
    private Map<Integer, Integer> agruparProductosPorId(List<Solicitud> solicitudes) {
        return solicitudes.stream()
                .flatMap(s -> s.getProductos().stream())
                .collect(Collectors.groupingBy(
                        sp -> sp.getProducto().getIdProducto(),
                        Collectors.summingInt(SolicitudProducto::getCantidadSolicitada)));
    }

    /**
     * Convierte mapa de productos agrupados a lista de DTOs.
     *
     * @param productosAgrupados Mapa de ID → cantidad
     * @return Lista de ConsolidacionProductoDTO
     */
    private List<ConsolidacionProductoDTO> convertirADTOs(Map<Integer, Integer> productosAgrupados) {
        return productosAgrupados.entrySet().stream()
                .map(this::crearProductoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un DTO de producto consolidado.
     *
     * @param entry Entry con ID y cantidad total
     * @return ConsolidacionProductoDTO
     */
    private ConsolidacionProductoDTO crearProductoDTO(Map.Entry<Integer, Integer> entry) {
        ConsolidacionProductoDTO dto = new ConsolidacionProductoDTO();
        dto.setIdProducto(entry.getKey());
        dto.setCantidadTotal(entry.getValue());
        return dto;
    }
}
