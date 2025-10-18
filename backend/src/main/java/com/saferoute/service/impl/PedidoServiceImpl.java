package com.saferoute.service.impl;

import com.saferoute.dto.*;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.interfaces.IPedidoService;
import com.saferoute.service.interfaces.ILogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final SolicitudRepository solicitudRepository;
    private final ILogService logService;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository, SolicitudRepository solicitudRepository,
            ILogService logService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.solicitudRepository = solicitudRepository;
        this.logService = logService;
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

        // Registrar creación de pedido en logs
        logService.registrarLog(idAdmin,
                "Pedido creado - ID: " + pedido.getIdPedido() +
                        ", Estado: " + pedido.getEstadoPedido() +
                        ", Productos: " + dto.getProductos().size());

        return dto;
    }

    @Override
    public PedidoDTO actualizarEstado(Integer idPedido, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        EstadoPedidoEnum estadoActual = pedido.getEstadoPedido();
        EstadoPedidoEnum estadoNuevo = EstadoPedidoEnum.valueOf(nuevoEstado);

        // Validar transición de estado
        validarTransicionEstado(estadoActual, estadoNuevo);

        pedido.setEstadoPedido(estadoNuevo);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Registrar cambio de estado en logs (usar registrarCambioEstadoPedido)
        Integer adminId = pedido.getAdmin().getIdUsuario();
        logService.registrarCambioEstadoPedido(adminId, idPedido,
                estadoActual.name(), estadoNuevo.name());

        return mapToDTO(pedidoGuardado);
    }

    /**
     * Valida que la transición entre estados de pedido sea válida según el flujo de
     * negocio
     * Flujo: CRT → ACT → (CRM|CRA) → [PRD → RCP] → RTA → ADU → ENT
     */
    private void validarTransicionEstado(EstadoPedidoEnum actual, EstadoPedidoEnum nuevo) {
        // Matriz de transiciones válidas
        Map<EstadoPedidoEnum, List<EstadoPedidoEnum>> transicionesPermitidas = new HashMap<>();

        // CRT (Creado) puede ir a: ACT (Activo) o CRM (Cerrado Manual)
        transicionesPermitidas.put(EstadoPedidoEnum.CRT,
                Arrays.asList(EstadoPedidoEnum.ACT, EstadoPedidoEnum.CRM));

        // ACT (Activo) puede ir a: CRM (Cerrado Manual), CRA (Cerrado Automático)
        transicionesPermitidas.put(EstadoPedidoEnum.ACT,
                Arrays.asList(EstadoPedidoEnum.CRM, EstadoPedidoEnum.CRA));

        // CRM (Cerrado Manual) puede ir a: PRD (Perdido), RTA (En Ruta)
        transicionesPermitidas.put(EstadoPedidoEnum.CRM,
                Arrays.asList(EstadoPedidoEnum.PRD, EstadoPedidoEnum.RTA));

        // CRA (Cerrado Automático) puede ir a: PRD (Perdido), RTA (En Ruta)
        transicionesPermitidas.put(EstadoPedidoEnum.CRA,
                Arrays.asList(EstadoPedidoEnum.PRD, EstadoPedidoEnum.RTA));

        // PRD (Perdido) puede ir a: RCP (Recuperado)
        transicionesPermitidas.put(EstadoPedidoEnum.PRD,
                Arrays.asList(EstadoPedidoEnum.RCP));

        // RCP (Recuperado) puede ir a: RTA (En Ruta)
        transicionesPermitidas.put(EstadoPedidoEnum.RCP,
                Arrays.asList(EstadoPedidoEnum.RTA));

        // RTA (En Ruta) puede ir a: ADU (Aduana), PRD (Perdido - por si se pierde en
        // ruta)
        transicionesPermitidas.put(EstadoPedidoEnum.RTA,
                Arrays.asList(EstadoPedidoEnum.ADU, EstadoPedidoEnum.PRD));

        // ADU (Aduana) puede ir a: ENT (Entregado), PRD (Perdido - por si se pierde en
        // aduana)
        transicionesPermitidas.put(EstadoPedidoEnum.ADU,
                Arrays.asList(EstadoPedidoEnum.ENT, EstadoPedidoEnum.PRD));

        // ENT (Entregado) es estado final - no puede cambiar
        transicionesPermitidas.put(EstadoPedidoEnum.ENT, Arrays.asList());

        // Validar que la transición sea permitida
        List<EstadoPedidoEnum> estadosPermitidos = transicionesPermitidas.get(actual);
        if (estadosPermitidos == null || !estadosPermitidos.contains(nuevo)) {
            throw new RuntimeException(String.format(
                    "Transición de estado inválida: no se puede cambiar de %s a %s. " +
                            "Estados permitidos desde %s: %s",
                    actual, nuevo, actual,
                    estadosPermitidos != null && !estadosPermitidos.isEmpty()
                            ? estadosPermitidos
                            : "ninguno (estado final)"));
        }
    }

    @Override
    public PedidoDTO actualizarPedido(Integer idPedido, PedidoDTO dto) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        EstadoPedidoEnum estadoActual = pedido.getEstadoPedido();

        // Si el pedido está ACTIVO (ACT), solo permitir modificar fecha de cierre
        if (estadoActual == EstadoPedidoEnum.ACT) {
            if (dto.getFechaCierre() != null) {
                pedido.setFechaCierre(dto.getFechaCierre());
            } else {
                throw new RuntimeException(
                        "El pedido está en estado ACTIVO. Solo se puede modificar la fecha de cierre");
            }
        }
        // Si el pedido está CREADO (CRT), permitir todas las modificaciones
        else if (estadoActual == EstadoPedidoEnum.CRT) {
            if (dto.getFechaCierre() != null) {
                pedido.setFechaCierre(dto.getFechaCierre());
            }
            if (dto.getEstadoPedido() != null) {
                pedido.setEstadoPedido(EstadoPedidoEnum.valueOf(dto.getEstadoPedido()));
            }
            // Productos se modifican con endpoints específicos
        }
        // Otros estados no permiten modificación
        else {
            throw new RuntimeException(
                    "No se puede modificar el pedido en estado: " + estadoActual);
        }

        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoDTO agregarProducto(Integer idPedido, ProductoPedidoDTO productoDTO) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Validar que el pedido esté en estado CRT (Creado)
        if (pedido.getEstadoPedido() != EstadoPedidoEnum.CRT) {
            throw new RuntimeException(
                    "Solo se pueden agregar productos a pedidos en estado CREADO (CRT). " +
                            "Estado actual: " + pedido.getEstadoPedido());
        }

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

        // Validar que el pedido esté en estado CRT (Creado)
        if (pedido.getEstadoPedido() != EstadoPedidoEnum.CRT) {
            throw new RuntimeException(
                    "Solo se pueden eliminar productos de pedidos en estado CREADO (CRT). " +
                            "Estado actual: " + pedido.getEstadoPedido());
        }

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

        // Validar que el pedido esté en estado CRT (Creado)
        if (pedido.getEstadoPedido() != EstadoPedidoEnum.CRT) {
            throw new RuntimeException(
                    "Solo se pueden modificar productos de pedidos en estado CREADO (CRT). " +
                            "Estado actual: " + pedido.getEstadoPedido());
        }

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

        // Cambiar estado del pedido a cancelado
        pedido.setEstadoPedido(EstadoPedidoEnum.CRM);
        pedidoRepository.save(pedido);

        // Buscar todas las solicitudes asociadas a este pedido y cancelarlas
        List<Solicitud> solicitudes = solicitudRepository.findByPedido_IdPedido(idPedido);
        for (Solicitud solicitud : solicitudes) {
            // Cancelar solo si no están ya canceladas
            if (solicitud.getEstadoSolicitud() != EstadoSolicitudEnum.CAN) {
                solicitud.setEstadoSolicitud(EstadoSolicitudEnum.CAN);
                solicitudRepository.save(solicitud);
            }
        }
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
