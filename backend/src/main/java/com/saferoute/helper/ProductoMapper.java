package com.saferoute.helper;

import com.saferoute.dto.ProductoDTO;
import com.saferoute.model.Producto;
import org.springframework.stereotype.Component;

/**
 * Helper para conversión entre entidades Producto y DTOs.
 */
@Component
public class ProductoMapper {

    /**
     * Convierte una entidad Producto a ProductoDTO.
     *
     * @param producto Entidad a convertir
     * @return DTO con los datos del producto
     */
    public ProductoDTO toDTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(producto.getIdProducto());
        dto.setNombreProducto(producto.getNombreProducto());
        dto.setTipoProducto(producto.getTipoProducto());
        dto.setDescripcionProducto(producto.getDescripcionProducto());
        dto.setPrecioUnitario(producto.getPrecioUnitario());
        dto.setCostoUnitario(producto.getCostoUnitario());
        dto.setUrlImagen(producto.getUrlImagen());
        return dto;
    }

    /**
     * Actualiza una entidad Producto con los datos de un DTO.
     * Solo actualiza campos no nulos del DTO.
     *
     * @param producto Entidad a actualizar
     * @param dto      DTO con los datos nuevos
     */
    public void updateFromDTO(Producto producto, ProductoDTO dto) {
        if (dto.getNombreProducto() != null) {
            producto.setNombreProducto(dto.getNombreProducto());
        }
        if (dto.getTipoProducto() != null) {
            producto.setTipoProducto(dto.getTipoProducto());
        }
        if (dto.getDescripcionProducto() != null) {
            producto.setDescripcionProducto(dto.getDescripcionProducto());
        }
        if (dto.getPrecioUnitario() != null) {
            producto.setPrecioUnitario(dto.getPrecioUnitario());
        }
        if (dto.getCostoUnitario() != null) {
            producto.setCostoUnitario(dto.getCostoUnitario());
        }
        if (dto.getUrlImagen() != null) {
            producto.setUrlImagen(dto.getUrlImagen());
        }
    }

    /**
     * Crea una nueva entidad Producto a partir de un DTO.
     *
     * @param dto DTO con los datos del producto
     * @return Nueva entidad Producto
     */
    public Producto toEntity(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setNombreProducto(dto.getNombreProducto());
        producto.setTipoProducto(dto.getTipoProducto());
        producto.setDescripcionProducto(dto.getDescripcionProducto());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setCostoUnitario(dto.getCostoUnitario());
        producto.setUrlImagen(dto.getUrlImagen());
        return producto;
    }
}
