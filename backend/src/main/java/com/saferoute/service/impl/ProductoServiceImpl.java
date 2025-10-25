package com.saferoute.service.impl;

import com.saferoute.constants.ProductoConstants;
import com.saferoute.dto.ProductoDTO;
import com.saferoute.exception.ProductoBusinessException;
import com.saferoute.helper.ProductoMapper;
import com.saferoute.helper.UsuarioAutenticadoHelper;
import com.saferoute.model.Producto;
import com.saferoute.repository.ProductoRepository;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.service.interfaces.IProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de productos.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final ILogService logService;
    private final ProductoMapper productoMapper;
    private final UsuarioAutenticadoHelper usuarioHelper;

    @Override
    public ProductoDTO crearProducto(ProductoDTO dto) {
        Producto producto = productoMapper.toEntity(dto);
        producto = productoRepository.save(producto);

        registrarLogCreacion(producto);

        return productoMapper.toDTO(producto);
    }

    private void registrarLogCreacion(Producto producto) {
        Integer usuarioId = usuarioHelper.obtenerUsuarioAutenticadoId();
        if (usuarioId != null) {
            log.info(ProductoConstants.LOG_PRODUCTO_CREADO,
                    producto.getIdProducto(),
                    producto.getNombreProducto(),
                    producto.getPrecioUnitario());

            logService.registrarLog(usuarioId,
                    String.format("Producto creado - ID: %d, Nombre: %s, Precio: $%s",
                            producto.getIdProducto(),
                            producto.getNombreProducto(),
                            producto.getPrecioUnitario()));
        }
    }

    @Override
    public ProductoDTO actualizarProducto(Integer id, ProductoDTO dto) {
        Producto producto = buscarProductoPorId(id);

        productoMapper.updateFromDTO(producto, dto);
        producto = productoRepository.save(producto);

        registrarLogActualizacion(producto);

        return productoMapper.toDTO(producto);
    }

    private void registrarLogActualizacion(Producto producto) {
        Integer usuarioId = usuarioHelper.obtenerUsuarioAutenticadoId();
        if (usuarioId != null) {
            log.info(ProductoConstants.LOG_PRODUCTO_ACTUALIZADO,
                    producto.getIdProducto(),
                    producto.getNombreProducto());

            logService.registrarLog(usuarioId,
                    String.format("Producto actualizado - ID: %d, Nombre: %s",
                            producto.getIdProducto(),
                            producto.getNombreProducto()));
        }
    }

    @Override
    public void eliminarProducto(Integer id) {
        Producto producto = buscarProductoPorId(id);

        // Soft delete - cambiar estado a INACTIVO
        producto.setEstadoProducto(ProductoConstants.ESTADO_INACTIVO);
        productoRepository.save(producto);

        registrarLogEliminacion(producto);
    }

    private void registrarLogEliminacion(Producto producto) {
        Integer usuarioId = usuarioHelper.obtenerUsuarioAutenticadoId();
        if (usuarioId != null) {
            log.info(ProductoConstants.LOG_PRODUCTO_ELIMINADO,
                    producto.getIdProducto(),
                    producto.getNombreProducto());

            logService.registrarLog(usuarioId,
                    String.format("Producto eliminado (soft delete) - ID: %d, Nombre: %s",
                            producto.getIdProducto(),
                            producto.getNombreProducto()));
        }
    }

    @Override
    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream()
                .filter(this::esProductoActivo)
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

    private boolean esProductoActivo(Producto producto) {
        return ProductoConstants.ESTADO_ACTIVO.equals(producto.getEstadoProducto());
    }

    @Override
    public ProductoDTO obtenerProductoPorId(Integer id) {
        Producto producto = buscarProductoPorId(id);
        return productoMapper.toDTO(producto);
    }

    @Override
    public ProductoDTO buscarProductoPorNombre(String nombreProducto) {
        Producto producto = productoRepository.findByNombreProducto(nombreProducto)
                .orElseThrow(() -> new ProductoBusinessException(
                        String.format(ProductoConstants.ERROR_PRODUCTO_NO_ENCONTRADO_POR_NOMBRE, nombreProducto)));
        return productoMapper.toDTO(producto);
    }

    /**
     * Busca un producto por ID o lanza excepción si no existe.
     */
    private Producto buscarProductoPorId(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoBusinessException(
                        String.format(ProductoConstants.ERROR_PRODUCTO_NO_ENCONTRADO_POR_ID, id)));
    }
}
