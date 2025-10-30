package com.saferoute.service.impl;

import com.saferoute.constants.ProductoConstants;
import com.saferoute.dto.ProductoDTO;
import com.saferoute.exception.ProductoBusinessException;
import com.saferoute.helper.ProductoMapper;
import com.saferoute.helper.UsuarioAutenticadoHelper;
import com.saferoute.model.Producto;
import com.saferoute.repository.ProductoRepository;
import com.saferoute.service.interfaces.IFirebaseStorageService;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.service.interfaces.IProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired(required = false)
    private IFirebaseStorageService firebaseStorageService;

    @Override
    public ProductoDTO crearProducto(ProductoDTO dto) {
        Producto producto = productoMapper.toEntity(dto);
        producto = productoRepository.save(producto);

        registrarLogCreacion(producto);

        return productoMapper.toDTO(producto);
    }

    @Override
    public ProductoDTO crearProductoConImagen(ProductoDTO dto, MultipartFile imagen) {
        // Si se proporciona imagen, subirla a Firebase
        if (imagen != null && !imagen.isEmpty()) {
            if (firebaseStorageService == null) {
                throw new ProductoBusinessException(
                        "Firebase Storage no está disponible. No se puede subir la imagen.");
            }

            // Crear el producto primero para obtener el ID
            Producto producto = productoMapper.toEntity(dto);
            producto = productoRepository.save(producto);

            try {
                // Subir la imagen a Firebase usando el ID del producto
                String urlImagen = firebaseStorageService.subirImagenProducto(imagen, producto.getIdProducto());
                producto.setUrlImagen(urlImagen);
                producto = productoRepository.save(producto);

                log.info("Imagen subida exitosamente para producto ID: {}, URL: {}",
                        producto.getIdProducto(), urlImagen);

            } catch (Exception e) {
                log.error("Error al subir imagen para producto ID: {}", producto.getIdProducto(), e);
                // El producto ya fue creado, solo advertimos del error de imagen
                log.warn("Producto creado sin imagen debido a error en Firebase Storage");
            }

            registrarLogCreacion(producto);
            return productoMapper.toDTO(producto);
        } else {
            // Si no hay imagen, crear producto normalmente
            return crearProducto(dto);
        }
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

    @Override
    public ProductoDTO actualizarProductoConImagen(Integer id, ProductoDTO dto, MultipartFile nuevaImagen) {
        Producto producto = buscarProductoPorId(id);
        String urlImagenAnterior = producto.getUrlImagen();

        // Actualizar datos básicos del producto
        productoMapper.updateFromDTO(producto, dto);

        // Si se proporciona nueva imagen, actualizarla en Firebase
        if (nuevaImagen != null && !nuevaImagen.isEmpty()) {
            if (firebaseStorageService == null) {
                throw new ProductoBusinessException(
                        "Firebase Storage no está disponible. No se puede actualizar la imagen.");
            }

            try {
                // Actualizar imagen (esto elimina la anterior y sube la nueva)
                String nuevaUrl = firebaseStorageService.actualizarImagenProducto(
                        nuevaImagen, id, urlImagenAnterior);
                producto.setUrlImagen(nuevaUrl);

                log.info("Imagen actualizada exitosamente para producto ID: {}, Nueva URL: {}",
                        id, nuevaUrl);

            } catch (Exception e) {
                log.error("Error al actualizar imagen para producto ID: {}", id, e);
                throw new ProductoBusinessException(
                        "Error al actualizar la imagen del producto: " + e.getMessage());
            }
        }

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

        // Eliminar imagen de Firebase si existe
        if (producto.getUrlImagen() != null && !producto.getUrlImagen().isEmpty()) {
            if (firebaseStorageService != null) {
                try {
                    firebaseStorageService.eliminarImagen(producto.getUrlImagen());
                    log.info("Imagen eliminada de Firebase para producto ID: {}", id);
                } catch (Exception e) {
                    log.warn("Error al eliminar imagen de Firebase para producto ID: {}", id, e);
                    // Continuamos con el soft delete aunque falle la eliminación de la imagen
                }
            }
        }

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
