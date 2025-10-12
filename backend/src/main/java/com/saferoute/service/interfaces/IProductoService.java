package com.saferoute.service.interfaces;

import com.saferoute.dto.ProductoDTO;
import java.util.List;

public interface IProductoService {
    ProductoDTO crearProducto(ProductoDTO productoDTO);
    ProductoDTO actualizarProducto(Integer id, ProductoDTO productoDTO);
    void eliminarProducto(Integer id);
    List<ProductoDTO> listarProductos();
    ProductoDTO obtenerProductoPorId(Integer id);
}
