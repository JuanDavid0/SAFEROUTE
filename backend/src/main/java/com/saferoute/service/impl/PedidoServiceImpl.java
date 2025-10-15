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
        pedido.setFechaCierre(dto.getFechaCierre());
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
