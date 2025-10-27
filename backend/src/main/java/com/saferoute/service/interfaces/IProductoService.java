package com.saferoute.service.interfaces;

import com.saferoute.dto.ProductoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductoService {
    ProductoDTO crearProducto(ProductoDTO productoDTO);

    ProductoDTO crearProductoConImagen(ProductoDTO productoDTO, MultipartFile imagen);

    ProductoDTO actualizarProducto(Integer id, ProductoDTO productoDTO);

    ProductoDTO actualizarProductoConImagen(Integer id, ProductoDTO productoDTO, MultipartFile imagen);

    void eliminarProducto(Integer id);

    List<ProductoDTO> listarProductos();

    ProductoDTO obtenerProductoPorId(Integer id);

    ProductoDTO buscarProductoPorNombre(String nombreProducto);
}
