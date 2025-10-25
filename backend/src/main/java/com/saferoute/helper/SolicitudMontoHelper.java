package com.saferoute.helper;

import com.saferoute.model.SolicitudProducto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Helper para cálculos de montos en solicitudes de WhatsApp.
 */
@Component
public class SolicitudMontoHelper {

    /**
     * Calcula el monto total de una colección de productos de solicitud.
     *
     * @param productos Colección de productos de la solicitud (puede ser List o
     *                  Set)
     * @return Monto total calculado
     */
    public BigDecimal calcularMontoTotal(Collection<SolicitudProducto> productos) {
        return productos.stream()
                .map(this::calcularMontoProducto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el monto de un solo producto (precio × cantidad).
     *
     * @param producto Producto de la solicitud
     * @return Monto del producto
     */
    private BigDecimal calcularMontoProducto(SolicitudProducto producto) {
        return producto.getPrecio()
                .multiply(BigDecimal.valueOf(producto.getCantidadSolicitada()));
    }
}
