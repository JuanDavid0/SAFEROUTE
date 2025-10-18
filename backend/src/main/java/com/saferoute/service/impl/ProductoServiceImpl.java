package com.saferoute.service.impl;

import com.saferoute.dto.ProductoDTO;
import com.saferoute.model.Producto;
import com.saferoute.model.Usuario;
import com.saferoute.repository.ProductoRepository;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.service.interfaces.IProductoService;
import com.saferoute.service.interfaces.ILogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ILogService logService;

    public ProductoServiceImpl(ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            ILogService logService) {
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.logService = logService;
    }

    /**
     * Obtiene el ID del usuario autenticado actual desde el contexto de seguridad
     */
    private Integer obtenerUsuarioAutenticadoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String cedula = auth.getName();
            return usuarioRepository.findByCedula(cedula)
                    .map(Usuario::getIdUsuario)
                    .orElse(null);
        }
        return null;
    }

    @Override
    public ProductoDTO crearProducto(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setNombreProducto(dto.getNombreProducto());
        producto.setTipoProducto(dto.getTipoProducto());
        producto.setDescripcionProducto(dto.getDescripcionProducto());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setCostoUnitario(dto.getCostoUnitario());
        producto.setUrlImagen(dto.getUrlImagen());
        productoRepository.save(producto);
        dto.setIdProducto(producto.getIdProducto());

        // Registrar creación de producto en logs
        Integer usuarioId = obtenerUsuarioAutenticadoId();
        if (usuarioId != null) {
            logService.registrarLog(usuarioId,
                    "Producto creado - ID: " + producto.getIdProducto() +
                            ", Nombre: " + producto.getNombreProducto() +
                            ", Precio: $" + producto.getPrecioUnitario());
        }

        return dto;
    }

    @Override
    public ProductoDTO actualizarProducto(Integer id, ProductoDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Actualización parcial - solo campos no nulos
        if (dto.getNombreProducto() != null) {
            producto.setNombreProducto(dto.getNombreProducto());
            dto.setNombreProducto(producto.getNombreProducto());
        }
        if (dto.getTipoProducto() != null) {
            producto.setTipoProducto(dto.getTipoProducto());
            dto.setTipoProducto(producto.getTipoProducto());
        }
        if (dto.getDescripcionProducto() != null) {
            producto.setDescripcionProducto(dto.getDescripcionProducto());
            dto.setDescripcionProducto(producto.getDescripcionProducto());
        }
        if (dto.getPrecioUnitario() != null) {
            producto.setPrecioUnitario(dto.getPrecioUnitario());
            dto.setPrecioUnitario(producto.getPrecioUnitario());
        }
        if (dto.getCostoUnitario() != null) {
            producto.setCostoUnitario(dto.getCostoUnitario());
            dto.setCostoUnitario(producto.getCostoUnitario());
        }
        if (dto.getUrlImagen() != null) {
            producto.setUrlImagen(dto.getUrlImagen());

        }

        producto = productoRepository.save(producto);
        dto.setIdProducto(producto.getIdProducto());

        // Registrar actualización de producto en logs
        Integer usuarioId = obtenerUsuarioAutenticadoId();
        if (usuarioId != null) {
            logService.registrarLog(usuarioId,
                    "Producto actualizado - ID: " + producto.getIdProducto() +
                            ", Nombre: " + producto.getNombreProducto());
        }

        return dto;
    }

    @Override
    public void eliminarProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Soft delete - cambiar estado a INACTIVO
        producto.setEstadoProducto("INACTIVO");
        productoRepository.save(producto);

        // Registrar eliminación (soft delete) de producto en logs
        Integer usuarioId = obtenerUsuarioAutenticadoId();
        if (usuarioId != null) {
            logService.registrarLog(usuarioId,
                    "Producto eliminado (soft delete) - ID: " + producto.getIdProducto() +
                            ", Nombre: " + producto.getNombreProducto());
        }
    }

    @Override
    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream()
                .filter(p -> "ACTIVO".equals(p.getEstadoProducto())) // Solo productos activos
                .map(p -> {
                    ProductoDTO dto = new ProductoDTO();
                    dto.setIdProducto(p.getIdProducto());
                    dto.setNombreProducto(p.getNombreProducto());
                    dto.setTipoProducto(p.getTipoProducto());
                    dto.setDescripcionProducto(p.getDescripcionProducto());
                    dto.setPrecioUnitario(p.getPrecioUnitario());
                    dto.setCostoUnitario(p.getCostoUnitario());
                    dto.setUrlImagen(p.getUrlImagen());
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
        dto.setUrlImagen(p.getUrlImagen());
        return dto;
    }

    @Override
    public ProductoDTO buscarProductoPorNombre(String nombreProducto) {
        Producto p = productoRepository.findByNombreProducto(nombreProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con nombre: " + nombreProducto));
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(p.getIdProducto());
        dto.setNombreProducto(p.getNombreProducto());
        dto.setTipoProducto(p.getTipoProducto());
        dto.setDescripcionProducto(p.getDescripcionProducto());
        dto.setPrecioUnitario(p.getPrecioUnitario());
        dto.setCostoUnitario(p.getCostoUnitario());
        dto.setUrlImagen(p.getUrlImagen());
        return dto;
    }
}
