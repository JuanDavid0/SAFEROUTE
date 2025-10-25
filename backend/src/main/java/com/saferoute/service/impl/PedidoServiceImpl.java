package com.saferoute.service.impl;

import com.saferoute.constants.PedidoConstants;
import com.saferoute.dto.*;
import com.saferoute.exception.PedidoBusinessException;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.helper.ProductoPedidoHelper;
import com.saferoute.service.interfaces.IPedidoService;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.service.interfaces.IWhatsAppService;
import com.saferoute.service.validator.PedidoValidator;
import org.hashids.Hashids;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de Pedidos
 * Refactorizado siguiendo principios SOLID y buenas prácticas
 */
@Service
@Transactional
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final SolicitudRepository solicitudRepository;
    private final ILogService logService;
    private final IWhatsAppService whatsAppService;
    private final Hashids hashids;
    private final PedidoValidator validator;
    private final ProductoPedidoHelper helper;

    public PedidoServiceImpl(
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            SolicitudRepository solicitudRepository,
            ILogService logService,
            IWhatsAppService whatsAppService,
            Hashids hashids,
            PedidoValidator validator,
            ProductoPedidoHelper helper) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.solicitudRepository = solicitudRepository;
        this.logService = logService;
        this.whatsAppService = whatsAppService;
        this.hashids = hashids;
        this.validator = validator;
        this.helper = helper;
    }

    @Override
    public PedidoDTO crearPedido(PedidoDTO dto, Integer idAdmin) {
        Usuario admin = usuarioRepository.findById(idAdmin)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_ADMIN_NO_ENCONTRADO));

        Pedido pedido = construirNuevoPedido(admin, dto);
        agregarProductosAPedido(pedido, dto.getProductos());

        pedidoRepository.save(pedido);
        actualizarDTOConDatosPersistidos(dto, pedido);

        registrarCreacionPedido(idAdmin, pedido, dto.getProductos().size());
        notificarSiPedidoActivo(pedido);

        return dto;
    }

    private Pedido construirNuevoPedido(Usuario admin, PedidoDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setAdmin(admin);
        pedido.setEstadoPedido(EstadoPedidoEnum.CRT);
        pedido.setFechaCierre(dto.getFechaCierre());
        return pedido;
    }

    private void agregarProductosAPedido(Pedido pedido, List<ProductoPedidoDTO> productosDTO) {
        for (ProductoPedidoDTO ppDTO : productosDTO) {
            Producto producto = productoRepository.findById(ppDTO.getIdProducto())
                    .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PRODUCTO_NO_ENCONTRADO));

            ProductoPedido pp = helper.crearProductoPedido(
                    pedido,
                    producto,
                    ppDTO.getCantidadMin(),
                    ppDTO.getCantidadMax());

            pedido.getProductos().add(pp);
        }
    }

    private void actualizarDTOConDatosPersistidos(PedidoDTO dto, Pedido pedido) {
        dto.setIdPedido(pedido.getIdPedido());
        dto.setIdAdmin(pedido.getAdmin().getIdUsuario());
        dto.setEstadoPedido(pedido.getEstadoPedido().name());
        dto.setFechaCreado(pedido.getFechaCreado());
        dto.setFechaCierre(pedido.getFechaCierre());
    }

    private void registrarCreacionPedido(Integer adminId, Pedido pedido, int cantidadProductos) {
        logService.registrarLog(adminId, String.format(
                PedidoConstants.LOG_PEDIDO_CREADO,
                pedido.getIdPedido(),
                pedido.getEstadoPedido(),
                cantidadProductos));
    }

    private void notificarSiPedidoActivo(Pedido pedido) {
        if (pedido.getEstadoPedido() == EstadoPedidoEnum.ACT) {
            whatsAppService.notificarNuevoPedidoActivo(pedido);
        }
    }

    @Override
    public PedidoDTO actualizarEstado(Integer idPedido, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO));

        EstadoPedidoEnum estadoActual = pedido.getEstadoPedido();
        EstadoPedidoEnum estadoNuevo = EstadoPedidoEnum.valueOf(nuevoEstado);

        // Validar transición de estado usando validator
        validator.validarTransicionEstado(estadoActual, estadoNuevo);

        pedido.setEstadoPedido(estadoNuevo);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        registrarCambioEstado(pedido, estadoActual, estadoNuevo);
        notificarCambioEstado(pedidoGuardado, estadoActual);
        procesarActivacionPedido(idPedido, pedidoGuardado, estadoActual, estadoNuevo);

        return mapToDTO(pedidoGuardado);
    }

    private void registrarCambioEstado(Pedido pedido, EstadoPedidoEnum estadoActual,
            EstadoPedidoEnum estadoNuevo) {
        Integer adminId = pedido.getAdmin().getIdUsuario();
        logService.registrarCambioEstadoPedido(
                adminId,
                pedido.getIdPedido(),
                estadoActual.name(),
                estadoNuevo.name());
    }

    private void notificarCambioEstado(Pedido pedido, EstadoPedidoEnum estadoActual) {
        whatsAppService.notificarCambioEstadoPedido(pedido, estadoActual.name());
    }

    private void procesarActivacionPedido(Integer idPedido, Pedido pedido,
            EstadoPedidoEnum estadoActual, EstadoPedidoEnum estadoNuevo) {
        if (estadoNuevo == EstadoPedidoEnum.ACT && estadoActual != EstadoPedidoEnum.ACT) {
            generarUrlHash(idPedido);
            whatsAppService.notificarNuevoPedidoActivo(pedido);
        }
    }

    @Override
    public PedidoDTO actualizarPedido(Integer idPedido, PedidoDTO dto) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO));

        EstadoPedidoEnum estadoActual = pedido.getEstadoPedido();

        if (estadoActual == EstadoPedidoEnum.ACT) {
            actualizarPedidoActivo(pedido, dto);
        } else if (estadoActual == EstadoPedidoEnum.CRT) {
            actualizarPedidoCreado(pedido, dto);
        } else {
            throw new PedidoBusinessException(String.format(
                    PedidoConstants.ERROR_PEDIDO_NO_MODIFICABLE,
                    estadoActual));
        }

        return mapToDTO(pedidoRepository.save(pedido));
    }

    private void actualizarPedidoActivo(Pedido pedido, PedidoDTO dto) {
        if (dto.getFechaCierre() != null) {
            pedido.setFechaCierre(dto.getFechaCierre());
        } else {
            throw new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_ACTIVO_SOLO_FECHA);
        }
    }

    private void actualizarPedidoCreado(Pedido pedido, PedidoDTO dto) {
        if (dto.getFechaCierre() != null) {
            pedido.setFechaCierre(dto.getFechaCierre());
        }
        if (dto.getEstadoPedido() != null) {
            pedido.setEstadoPedido(EstadoPedidoEnum.valueOf(dto.getEstadoPedido()));
        }
        // Los productos se modifican con endpoints específicos
    }

    @Override
    public PedidoDTO agregarProducto(Integer idPedido, ProductoPedidoDTO productoDTO) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO));

        validator.validarPedidoEstadoCreado(pedido, "agregar");

        Producto producto = productoRepository.findById(productoDTO.getIdProducto())
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PRODUCTO_NO_ENCONTRADO));

        helper.validarProductoNoExiste(pedido, productoDTO.getIdProducto());

        ProductoPedido pp = helper.crearProductoPedido(
                pedido,
                producto,
                productoDTO.getCantidadMin(),
                productoDTO.getCantidadMax());

        pedido.getProductos().add(pp);
        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoDTO eliminarProducto(Integer idPedido, Integer idProducto) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO));

        validator.validarPedidoEstadoCreado(pedido, "eliminar");

        ProductoPedido pp = helper.buscarProductoEnPedido(pedido, idProducto);
        pedido.getProductos().remove(pp);

        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoDTO modificarProducto(Integer idPedido, Integer idProducto, ProductoPedidoDTO productoDTO) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO));

        validator.validarPedidoEstadoCreado(pedido, "modificar");

        ProductoPedido pp = helper.buscarProductoEnPedido(pedido, idProducto);
        helper.actualizarCantidades(pp, productoDTO);

        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public void cancelarPedido(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO));

        cambiarEstadoPedido(pedido, EstadoPedidoEnum.CRM);
        cancelarSolicitudesAsociadas(idPedido);
        whatsAppService.notificarCancelacionPedido(pedido);
    }

    private void cambiarEstadoPedido(Pedido pedido, EstadoPedidoEnum nuevoEstado) {
        pedido.setEstadoPedido(nuevoEstado);
        pedidoRepository.save(pedido);
    }

    private void cancelarSolicitudesAsociadas(Integer idPedido) {
        List<Solicitud> solicitudes = solicitudRepository.findByPedido_IdPedido(idPedido);
        solicitudes.stream()
                .filter(solicitud -> solicitud.getEstadoSolicitud() != EstadoSolicitudEnum.CAN)
                .forEach(solicitud -> {
                    solicitud.setEstadoSolicitud(EstadoSolicitudEnum.CAN);
                    solicitudRepository.save(solicitud);
                });
    }

    @Override
    public List<PedidoDTO> listarPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PedidoDTO obtenerPedidoPorId(Integer id) {
        return mapToDTO(pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO)));
    }

    @Override
    public String generarUrlHash(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO));

        validator.validarPedidoEstadoActivo(pedido);

        // Si ya tiene hash, devolverlo (idempotente)
        if (pedido.getUrlHash() != null && !pedido.getUrlHash().isEmpty()) {
            return pedido.getUrlHash();
        }

        String hash = generarHashDeId(idPedido);
        guardarHash(pedido, hash);
        registrarGeneracionHash(pedido, hash);

        return hash;
    }

    private String generarHashDeId(Integer idPedido) {
        // Genera hash usando Hashids - convierte el ID numérico en string ofuscado
        // Ejemplo: ID 123 -> "5N6y2Kl" (reversible con la misma salt)
        return hashids.encode(idPedido.longValue());
    }

    private void guardarHash(Pedido pedido, String hash) {
        pedido.setUrlHash(hash);
        pedidoRepository.save(pedido);
    }

    private void registrarGeneracionHash(Pedido pedido, String hash) {
        logService.registrarLog(
                pedido.getAdmin().getIdUsuario(),
                String.format(PedidoConstants.LOG_HASH_GENERADO, pedido.getIdPedido(), hash));
    }

    @Override
    public PedidoDTO obtenerPedidoPorHash(String hash) {
        validarHashNoVacio(hash);

        Integer idPedido = decodificarHash(hash);
        Pedido pedido = obtenerPedido(idPedido);

        validator.validarPedidoEstadoActivo(pedido);

        return mapToDTO(pedido);
    }

    private void validarHashNoVacio(String hash) {
        if (hash == null || hash.isEmpty()) {
            throw new PedidoBusinessException(PedidoConstants.ERROR_HASH_VACIO);
        }
    }

    private Integer decodificarHash(String hash) {
        long[] ids = hashids.decode(hash);
        if (ids.length == 0) {
            throw new PedidoBusinessException(PedidoConstants.ERROR_HASH_INVALIDO);
        }
        return (int) ids[0];
    }

    private Pedido obtenerPedido(Integer idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new PedidoBusinessException(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO));
    }

    private PedidoDTO mapToDTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setIdPedido(pedido.getIdPedido());
        dto.setIdAdmin(pedido.getAdmin().getIdUsuario());
        dto.setEstadoPedido(pedido.getEstadoPedido().name());
        dto.setFechaCreado(pedido.getFechaCreado());
        dto.setFechaCierre(pedido.getFechaCierre());
        dto.setUrlHash(pedido.getUrlHash());
        dto.setProductos(pedido.getProductos().stream()
                .map(this::mapProductoPedidoToDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private ProductoPedidoDTO mapProductoPedidoToDTO(ProductoPedido pp) {
        ProductoPedidoDTO ppDTO = new ProductoPedidoDTO();
        ppDTO.setIdProducto(pp.getProducto().getIdProducto());
        ppDTO.setCantidadMin(pp.getCantidadMin());
        ppDTO.setCantidadMax(pp.getCantidadMax());
        return ppDTO;
    }
}
