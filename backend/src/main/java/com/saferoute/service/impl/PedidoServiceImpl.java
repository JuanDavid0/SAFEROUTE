package com.saferoute.service.impl;

import com.saferoute.dto.*;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.repository.*;
import com.saferoute.service.interfaces.IPedidoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public PedidoDTO crearPedido(PedidoDTO dto, Integer idAdmin) {
        Usuario admin = usuarioRepository.findById(idAdmin)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setAdmin(admin);
        pedido.setEstadoPedido(EstadoPedidoEnum.CRT);
        pedido.setFechaCierre(dto.getFechaCierre());

        for (ProductoPedidoDTO ppDTO : dto.getProductos()) {
            Producto producto = productoRepository.findById(ppDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            ProductoPedido pp = new ProductoPedido();
            pp.setPedido(pedido);
            pp.setProducto(producto);
            pp.setCantidadMin(ppDTO.getCantidadMin());
            pp.setCantidadMax(ppDTO.getCantidadMax());

            pedido.getProductos().add(pp);
        }

        pedidoRepository.save(pedido);
        dto.setIdPedido(pedido.getIdPedido());
        dto.setIdAdmin(pedido.getAdmin().getIdUsuario());
        dto.setEstadoPedido(pedido.getEstadoPedido().name());
        dto.setFechaCreado(pedido.getFechaCreado());
        dto.setFechaCierre(pedido.getFechaCierre());
        return dto;
    }

    @Override
    public PedidoDTO actualizarEstado(Integer idPedido, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstadoPedido(EstadoPedidoEnum.valueOf(nuevoEstado));
        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoDTO actualizarPedido(Integer idPedido, PedidoDTO dto) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Actualizar fecha de cierre si se proporciona
        if (dto.getFechaCierre() != null) {
            pedido.setFechaCierre(dto.getFechaCierre());
        }

        // Actualizar estado si se proporciona
        if (dto.getEstadoPedido() != null) {
            pedido.setEstadoPedido(EstadoPedidoEnum.valueOf(dto.getEstadoPedido()));
        }

        // NO actualizar productos aquí - usar endpoints específicos

        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoDTO agregarProducto(Integer idPedido, ProductoPedidoDTO productoDTO) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Producto producto = productoRepository.findById(productoDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar si el producto ya existe en el pedido
        boolean productoExiste = pedido.getProductos().stream()
                .anyMatch(pp -> pp.getProducto().getIdProducto().equals(productoDTO.getIdProducto()));

        if (productoExiste) {
            throw new RuntimeException("El producto ya existe en este pedido. Use modificar para cambiar cantidades.");
        }

        ProductoPedido pp = new ProductoPedido();
        pp.setPedido(pedido);
        pp.setProducto(producto);
        pp.setCantidadMin(productoDTO.getCantidadMin());
        pp.setCantidadMax(productoDTO.getCantidadMax());

        pedido.getProductos().add(pp);
        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoDTO eliminarProducto(Integer idPedido, Integer idProducto) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        ProductoPedido pp = pedido.getProductos().stream()
                .filter(p -> p.getProducto().getIdProducto().equals(idProducto))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en este pedido"));

        pedido.getProductos().remove(pp);
        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoDTO modificarProducto(Integer idPedido, Integer idProducto, ProductoPedidoDTO productoDTO) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        ProductoPedido pp = pedido.getProductos().stream()
                .filter(p -> p.getProducto().getIdProducto().equals(idProducto))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en este pedido"));

        // Actualizar cantidades
        if (productoDTO.getCantidadMin() != null) {
            pp.setCantidadMin(productoDTO.getCantidadMin());
        }
        if (productoDTO.getCantidadMax() != null) {
            pp.setCantidadMax(productoDTO.getCantidadMax());
        }

        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public void cancelarPedido(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstadoPedido(EstadoPedidoEnum.CRM);
        pedidoRepository.save(pedido);
    }

    @Override
    public List<PedidoDTO> listarPedidos() {
        return pedidoRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public PedidoDTO obtenerPedidoPorId(Integer id) {
        return mapToDTO(pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado")));
    }

    private PedidoDTO mapToDTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setIdPedido(pedido.getIdPedido());
        dto.setIdAdmin(pedido.getAdmin().getIdUsuario());
        dto.setEstadoPedido(pedido.getEstadoPedido().name());
        dto.setFechaCreado(pedido.getFechaCreado());
        dto.setFechaCierre(pedido.getFechaCierre());
        dto.setProductos(pedido.getProductos().stream().map(pp -> {
            ProductoPedidoDTO ppDTO = new ProductoPedidoDTO();
            ppDTO.setIdProducto(pp.getProducto().getIdProducto());
            ppDTO.setCantidadMin(pp.getCantidadMin());
            ppDTO.setCantidadMax(pp.getCantidadMax());
            return ppDTO;
        }).collect(Collectors.toList()));
        return dto;
    }
}
