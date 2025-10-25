package com.saferoute.service.helper;

import com.saferoute.dto.ProductoPedidoDTO;
import com.saferoute.exception.PedidoBusinessException;
import com.saferoute.model.Pedido;
import com.saferoute.model.Producto;
import com.saferoute.model.ProductoPedido;
import org.springframework.stereotype.Component;

import static com.saferoute.constants.PedidoConstants.ERROR_PRODUCTO_YA_EXISTE;
import static com.saferoute.constants.PedidoConstants.ERROR_PRODUCTO_NO_EN_PEDIDO;

/**
 * Helper para operaciones comunes de ProductoPedido
 * Aplica el principio DRY (Don't Repeat Yourself)
 */
@Component
public class ProductoPedidoHelper {

    /**
     * Crea una nueva relación ProductoPedido
     */
    public ProductoPedido crearProductoPedido(Pedido pedido, Producto producto,
            Integer cantidadMin, Integer cantidadMax) {
        ProductoPedido pp = new ProductoPedido();
        pp.setPedido(pedido);
        pp.setProducto(producto);
        pp.setCantidadMin(cantidadMin);
        pp.setCantidadMax(cantidadMax);
        return pp;
    }

    /**
     * Verifica si un producto ya existe en el pedido
     */
    public boolean productoExisteEnPedido(Pedido pedido, Integer idProducto) {
        return pedido.getProductos().stream()
                .anyMatch(pp -> pp.getProducto().getIdProducto().equals(idProducto));
    }

    /**
     * Busca un ProductoPedido específico en el pedido
     */
    public ProductoPedido buscarProductoEnPedido(Pedido pedido, Integer idProducto) {
        return pedido.getProductos().stream()
                .filter(p -> p.getProducto().getIdProducto().equals(idProducto))
                .findFirst()
                .orElseThrow(() -> new PedidoBusinessException(ERROR_PRODUCTO_NO_EN_PEDIDO));
    }

    /**
     * Valida que el producto no exista antes de agregarlo
     */
    public void validarProductoNoExiste(Pedido pedido, Integer idProducto) {
        if (productoExisteEnPedido(pedido, idProducto)) {
            throw new PedidoBusinessException(ERROR_PRODUCTO_YA_EXISTE);
        }
    }

    /**
     * Actualiza las cantidades de un ProductoPedido
     */
    public void actualizarCantidades(ProductoPedido pp, ProductoPedidoDTO dto) {
        if (dto.getCantidadMin() != null) {
            pp.setCantidadMin(dto.getCantidadMin());
        }
        if (dto.getCantidadMax() != null) {
            pp.setCantidadMax(dto.getCantidadMax());
        }
    }
}
