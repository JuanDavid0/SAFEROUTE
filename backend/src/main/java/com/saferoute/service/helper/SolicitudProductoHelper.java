package com.saferoute.service.helper;

import com.saferoute.model.Producto;
import com.saferoute.model.Solicitud;
import com.saferoute.model.SolicitudProducto;
import org.springframework.stereotype.Component;

/**
 * Helper para operaciones comunes de SolicitudProducto
 * Aplica el principio DRY (Don't Repeat Yourself)
 */
@Component
public class SolicitudProductoHelper {

    /**
     * Crea una nueva relación SolicitudProducto
     */
    public SolicitudProducto crearSolicitudProducto(Solicitud solicitud, Producto producto, Integer cantidad) {
        SolicitudProducto sp = new SolicitudProducto();
        sp.setSolicitud(solicitud);
        sp.setProducto(producto);
        sp.setCantidadSolicitada(cantidad);
        sp.setPrecio(producto.getPrecioUnitario());
        return sp;
    }

    /**
     * Verifica si un producto ya existe en la solicitud
     */
    public boolean productoExisteEnSolicitud(Solicitud solicitud, Integer idProducto) {
        return solicitud.getProductos().stream()
                .anyMatch(sp -> sp.getProducto().getIdProducto().equals(idProducto));
    }

    /**
     * Verifica si un producto existe en el pedido
     */
    public boolean productoExisteEnPedido(Solicitud solicitud, Integer idProducto) {
        return solicitud.getPedido().getProductos().stream()
                .anyMatch(pp -> pp.getProducto().getIdProducto().equals(idProducto));
    }

    /**
     * Busca un SolicitudProducto específico en la solicitud
     */
    public SolicitudProducto buscarProductoEnSolicitud(Solicitud solicitud, Integer idProducto) {
        return solicitud.getProductos().stream()
                .filter(p -> p.getProducto().getIdProducto().equals(idProducto))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en esta solicitud"));
    }

    /**
     * Decrementa las modificaciones restantes de la solicitud
     */
    public void decrementarModificaciones(Solicitud solicitud) {
        solicitud.setModificacionesRestantes(solicitud.getModificacionesRestantes() - 1);
    }
}
