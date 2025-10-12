package com.saferoute.service.impl;

import com.saferoute.dto.ProductoDTO;
import com.saferoute.model.Producto;
import com.saferoute.repository.ProductoRepository;
import com.saferoute.service.interfaces.IProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public ProductoDTO crearProducto(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setNombreProducto(dto.getNombreProducto());
        producto.setTipoProducto(dto.getTipoProducto());
        producto.setDescripcionProducto(dto.getDescripcionProducto());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setCostoUnitario(dto.getCostoUnitario());
        productoRepository.save(producto);
        dto.setIdProducto(producto.getIdProducto());
        return dto;
    }

    @Override
    public ProductoDTO actualizarProducto(Integer id, ProductoDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setNombreProducto(dto.getNombreProducto());
        producto.setTipoProducto(dto.getTipoProducto());
        producto.setDescripcionProducto(dto.getDescripcionProducto());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setCostoUnitario(dto.getCostoUnitario());
        productoRepository.save(producto);
        return dto;
    }

    @Override
    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream().map(p -> {
            ProductoDTO dto = new ProductoDTO();
            dto.setIdProducto(p.getIdProducto());
            dto.setNombreProducto(p.getNombreProducto());
            dto.setTipoProducto(p.getTipoProducto());
            dto.setDescripcionProducto(p.getDescripcionProducto());
            dto.setPrecioUnitario(p.getPrecioUnitario());
            dto.setCostoUnitario(p.getCostoUnitario());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ProductoDTO obtenerProductoPorId(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(p.getIdProducto());
        dto.setNombreProducto(p.getNombreProducto());
        dto.setTipoProducto(p.getTipoProducto());
        dto.setDescripcionProducto(p.getDescripcionProducto());
        dto.setPrecioUnitario(p.getPrecioUnitario());
        dto.setCostoUnitario(p.getCostoUnitario());
        return dto;
    }
}
