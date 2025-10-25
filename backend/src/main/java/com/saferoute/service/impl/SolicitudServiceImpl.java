package com.saferoute.service.impl;

import com.saferoute.dto.*;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.interfaces.ISolicitudService;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.service.interfaces.IWhatsAppService;
import com.saferoute.repository.UsuarioRolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SolicitudServiceImpl implements ISolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ILogService logService;
    private final IWhatsAppService whatsAppService;

    public SolicitudServiceImpl(SolicitudRepository solicitudRepository, UsuarioRepository usuarioRepository,
            PedidoRepository pedidoRepository, ProductoRepository productoRepository, RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository, ILogService logService, IWhatsAppService whatsAppService) {
        this.usuarioRolRepository = usuarioRolRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.rolRepository = rolRepository;
        this.logService = logService;
        this.whatsAppService = whatsAppService;
    }

    @Override
    public SolicitudDTO crearSolicitud(SolicitudDTO dto, Integer idCliente) {
        Usuario cliente = usuarioRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Pedido pedido = pedidoRepository.findById(dto.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cliente);
        solicitud.setPedido(pedido);
        solicitud.setDireccionEntrega(dto.getDireccionEntrega());
        solicitud.setEstadoSolicitud(EstadoSolicitudEnum.PDP);

        for (SolicitudProductoDTO spDTO : dto.getProductos()) {
            Producto producto = productoRepository.findById(spDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            SolicitudProducto sp = new SolicitudProducto();
            sp.setSolicitud(solicitud);
            sp.setProducto(producto);
            sp.setCantidadSolicitada(spDTO.getCantidadSolicitada());
            // CALCULAR PRECIO AUTOMÁTICAMENTE del producto
            sp.setPrecio(producto.getPrecioUnitario());
            solicitud.getProductos().add(sp);
        }

        solicitudRepository.save(solicitud);

        // Registrar creación de solicitud en logs
        logService.registrarLog(idCliente,
                "Solicitud creada - ID: " + solicitud.getIdSolicitud() +
                        ", Pedido ID: " + pedido.getIdPedido() +
                        ", Productos: " + dto.getProductos().size());

        SolicitudDTO solicitudDTO = mapToDTO(solicitud);

        // Enviar notificación de WhatsApp con resumen de la solicitud
        whatsAppService.enviarResumenSolicitud(solicitudDTO);

        return solicitudDTO;
    }

    @Override
    public SolicitudDTO crearSolicitudClienteNuevo(SolicitudClienteDTO dto) {
        Usuario cliente = null;

        // Buscar usuario existente por cédula o teléfono
        if (usuarioRepository.findByCedula(dto.getCedula()).isPresent()) {
            cliente = usuarioRepository.findByCedula(dto.getCedula()).get();
        } else if (usuarioRepository.findByTelefono(dto.getTelefono()).isPresent()) {
            cliente = usuarioRepository.findByTelefono(dto.getTelefono()).get();
        }

        // Si no existe, crear nuevo usuario
        if (cliente == null) {
            Usuario nuevoCliente = new Usuario();
            nuevoCliente.setNombres(dto.getNombres());
            nuevoCliente.setApellidos(dto.getApellidos());
            nuevoCliente.setTelefono(dto.getTelefono());
            nuevoCliente.setCedula(dto.getCedula());
            nuevoCliente.setDireccion(dto.getDireccion());
            cliente = usuarioRepository.save(nuevoCliente);

            // Asignar rol "CLI"
            Rol rolCliente = rolRepository.findByTipoRol("CLI")
                    .orElseThrow(() -> new RuntimeException("Rol CLI no encontrado"));
            UsuarioRol usuarioRol = new UsuarioRol(rolCliente, cliente);
            usuarioRolRepository.save(usuarioRol);
        }

        // Crear la solicitud
        SolicitudDTO solicitudDTO = new SolicitudDTO();
        solicitudDTO.setIdPedido(dto.getIdPedido());
        solicitudDTO.setDireccionEntrega(dto.getDireccion());
        solicitudDTO.setProductos(dto.getProductos());
        return crearSolicitud(solicitudDTO, cliente.getIdUsuario());
    }

    @Override
    public SolicitudDTO modificarSolicitud(Integer idSolicitud, SolicitudModificacionDTO datos) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!solicitud.getEstadoSolicitud().equals(EstadoSolicitudEnum.PDP))
            throw new RuntimeException("Solo se puede modificar solicitudes pendientes de pago");

        // Verificar que no estemos a 5 días o menos del cierre
        LocalDate fechaCierre = solicitud.getPedido().getFechaCierre();
        if (fechaCierre != null) {
            LocalDate fechaLimite = fechaCierre.minusDays(5);
            if (LocalDate.now().isAfter(fechaLimite)) {
                throw new RuntimeException("No se pueden realizar modificaciones 5 días antes del cierre del pedido");
            }
        }

        // Verificar modificaciones restantes a nivel de solicitud
        if (solicitud.getModificacionesRestantes() <= 0) {
            throw new RuntimeException(
                    "Se alcanzó el número máximo de modificaciones para esta solicitud (3 modificaciones)");
        }

        // Modificar dirección de entrega
        if (datos.getDireccionEntrega() != null) {
            solicitud.setDireccionEntrega(datos.getDireccionEntrega());
        }

        // Modificar productos (nuevo formato con lista)
        if (datos.getProductos() != null && !datos.getProductos().isEmpty()) {
            for (SolicitudModificacionDTO.ProductoModificacionDTO prodMod : datos.getProductos()) {
                // Buscar el producto en la solicitud
                SolicitudProducto sp = solicitud.getProductos().stream()
                        .filter(s -> s.getProducto().getIdProducto().equals(prodMod.getIdProducto()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado en esta solicitud"));

                sp.setCantidadSolicitada(prodMod.getCantidadSolicitada());
            }
        }
        // Compatibilidad con formato antiguo (un solo producto)
        else if (datos.getNuevaCantidad() != null && !solicitud.getProductos().isEmpty()) {
            SolicitudProducto sp = solicitud.getProductos().iterator().next();
            sp.setCantidadSolicitada(datos.getNuevaCantidad());
        }

        // Decrementar modificaciones restantes a nivel de solicitud
        solicitud.setModificacionesRestantes(solicitud.getModificacionesRestantes() - 1);

        solicitudRepository.save(solicitud);
        return mapToDTO(solicitud);
    }

    @Override
    public SolicitudDTO agregarProducto(Integer idSolicitud, SolicitudProductoDTO productoDTO) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Validar que la solicitud esté en estado PDP
        if (solicitud.getEstadoSolicitud() != EstadoSolicitudEnum.PDP) {
            throw new RuntimeException(
                    "Solo se pueden agregar productos a solicitudes en estado Pendiente de Pago (PDP). " +
                            "Estado actual: " + solicitud.getEstadoSolicitud());
        }

        // Verificar ventana de modificación (5 días antes del cierre)
        LocalDate fechaCierre = solicitud.getPedido().getFechaCierre();
        if (fechaCierre != null) {
            LocalDate fechaLimite = fechaCierre.minusDays(5);
            if (LocalDate.now().isAfter(fechaLimite)) {
                throw new RuntimeException("No se pueden agregar productos 5 días antes del cierre del pedido");
            }
        }

        // Verificar modificaciones restantes a nivel de solicitud
        if (solicitud.getModificacionesRestantes() <= 0) {
            throw new RuntimeException(
                    "Se alcanzó el número máximo de modificaciones para esta solicitud (3 modificaciones)");
        }

        // Verificar que el producto exista
        Producto producto = productoRepository.findById(productoDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar que el producto esté en el pedido asociado
        boolean productoEnPedido = solicitud.getPedido().getProductos().stream()
                .anyMatch(pp -> pp.getProducto().getIdProducto().equals(productoDTO.getIdProducto()));

        if (!productoEnPedido) {
            throw new RuntimeException("El producto no está disponible en el pedido asociado");
        }

        // Verificar que el producto no exista ya en la solicitud
        boolean productoYaEnSolicitud = solicitud.getProductos().stream()
                .anyMatch(sp -> sp.getProducto().getIdProducto().equals(productoDTO.getIdProducto()));

        if (productoYaEnSolicitud) {
            throw new RuntimeException("El producto ya existe en esta solicitud. Use modificar cantidad en su lugar.");
        }

        // Agregar el producto a la solicitud
        SolicitudProducto sp = new SolicitudProducto();
        sp.setSolicitud(solicitud);
        sp.setProducto(producto);
        sp.setCantidadSolicitada(productoDTO.getCantidadSolicitada());
        sp.setPrecio(producto.getPrecioUnitario());

        solicitud.getProductos().add(sp);

        // Decrementar modificaciones restantes a nivel de solicitud
        solicitud.setModificacionesRestantes(solicitud.getModificacionesRestantes() - 1);

        solicitudRepository.save(solicitud);

        // Registrar log de agregación de producto
        logService.registrarLog(solicitud.getCliente().getIdUsuario(),
                "Producto agregado a solicitud - Solicitud ID: " + idSolicitud +
                        ", Producto: " + producto.getNombreProducto() +
                        ", Cantidad: " + productoDTO.getCantidadSolicitada() +
                        ", Modificaciones restantes: " + solicitud.getModificacionesRestantes());

        return mapToDTO(solicitud);
    }

    @Override
    public SolicitudDTO eliminarProducto(Integer idSolicitud, Integer idProducto) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Validar que la solicitud esté en estado PDP
        if (solicitud.getEstadoSolicitud() != EstadoSolicitudEnum.PDP) {
            throw new RuntimeException(
                    "Solo se pueden eliminar productos de solicitudes en estado Pendiente de Pago (PDP). " +
                            "Estado actual: " + solicitud.getEstadoSolicitud());
        }

        // Verificar ventana de modificación (5 días antes del cierre)
        LocalDate fechaCierre = solicitud.getPedido().getFechaCierre();
        if (fechaCierre != null) {
            LocalDate fechaLimite = fechaCierre.minusDays(5);
            if (LocalDate.now().isAfter(fechaLimite)) {
                throw new RuntimeException("No se pueden eliminar productos 5 días antes del cierre del pedido");
            }
        }

        // Verificar modificaciones restantes a nivel de solicitud
        if (solicitud.getModificacionesRestantes() <= 0) {
            throw new RuntimeException(
                    "Se alcanzó el número máximo de modificaciones para esta solicitud (3 modificaciones)");
        }

        // Verificar que la solicitud tenga al menos 2 productos
        if (solicitud.getProductos().size() <= 1) {
            throw new RuntimeException(
                    "No se puede eliminar el único producto de la solicitud. Cancele la solicitud en su lugar.");
        }

        // Buscar y eliminar el producto
        SolicitudProducto sp = solicitud.getProductos().stream()
                .filter(p -> p.getProducto().getIdProducto().equals(idProducto))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en esta solicitud"));

        String nombreProducto = sp.getProducto().getNombreProducto();
        solicitud.getProductos().remove(sp);

        // Decrementar modificaciones restantes a nivel de solicitud
        solicitud.setModificacionesRestantes(solicitud.getModificacionesRestantes() - 1);

        solicitudRepository.save(solicitud);

        // Registrar log de eliminación de producto
        logService.registrarLog(solicitud.getCliente().getIdUsuario(),
                "Producto eliminado de solicitud - Solicitud ID: " + idSolicitud +
                        ", Producto: " + nombreProducto +
                        ", Modificaciones restantes: " + solicitud.getModificacionesRestantes());

        return mapToDTO(solicitud);
    }

    @Override
    public SolicitudDTO modificarCantidadProducto(Integer idSolicitud, Integer idProducto, Integer nuevaCantidad) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Validar que la solicitud esté en estado PDP
        if (solicitud.getEstadoSolicitud() != EstadoSolicitudEnum.PDP) {
            throw new RuntimeException(
                    "Solo se pueden modificar productos de solicitudes en estado Pendiente de Pago (PDP). " +
                            "Estado actual: " + solicitud.getEstadoSolicitud());
        }

        // Verificar ventana de modificación (5 días antes del cierre)
        LocalDate fechaCierre = solicitud.getPedido().getFechaCierre();
        if (fechaCierre != null) {
            LocalDate fechaLimite = fechaCierre.minusDays(5);
            if (LocalDate.now().isAfter(fechaLimite)) {
                throw new RuntimeException("No se pueden modificar productos 5 días antes del cierre del pedido");
            }
        }

        // Verificar modificaciones restantes a nivel de solicitud
        if (solicitud.getModificacionesRestantes() <= 0) {
            throw new RuntimeException(
                    "Se alcanzó el número máximo de modificaciones para esta solicitud (3 modificaciones)");
        }

        // Buscar el producto en la solicitud
        SolicitudProducto sp = solicitud.getProductos().stream()
                .filter(p -> p.getProducto().getIdProducto().equals(idProducto))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en esta solicitud"));

        // Actualizar cantidad
        sp.setCantidadSolicitada(nuevaCantidad);

        // Decrementar modificaciones restantes a nivel de solicitud
        solicitud.setModificacionesRestantes(solicitud.getModificacionesRestantes() - 1);

        solicitudRepository.save(solicitud);

        return mapToDTO(solicitud);
    }

    @Override
    public void cancelarSolicitud(Integer idSolicitud) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Validar que la solicitud esté en estado PDP (Pendiente de Pago)
        if (solicitud.getEstadoSolicitud() != EstadoSolicitudEnum.PDP) {
            throw new RuntimeException(
                    "Solo se pueden cancelar solicitudes en estado Pendiente de Pago (PDP). " +
                            "Estado actual: " + solicitud.getEstadoSolicitud());
        }

        solicitud.setEstadoSolicitud(EstadoSolicitudEnum.CAN);
        solicitudRepository.save(solicitud);

        // Registrar cancelación de solicitud en logs
        Integer clienteId = solicitud.getCliente().getIdUsuario();
        logService.registrarLog(clienteId,
                "Solicitud cancelada - ID: " + solicitud.getIdSolicitud() +
                        ", Pedido ID: " + solicitud.getPedido().getIdPedido());
    }

    @Override
    public void cancelarSolicitudesVencidas() {
        // Buscar solicitudes cuyo pedido asociado tiene fecha_cierre vencida
        List<Solicitud> vencidas = solicitudRepository
                .findByPedidoFechaCierreBeforeAndEstadoSolicitud(LocalDate.now(), EstadoSolicitudEnum.PDP);
        vencidas.forEach(s -> s.setEstadoSolicitud(EstadoSolicitudEnum.CAN));
        solicitudRepository.saveAll(vencidas);
    }

    @Override
    public List<SolicitudDTO> listarSolicitudesCliente(Integer idCliente) {
        Usuario cliente = usuarioRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return solicitudRepository.findByCliente(cliente)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<SolicitudDTO> listarSolicitudesPorCedula(String cedula) {
        return solicitudRepository.findByCliente_Cedula(cedula)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public void cambiarEstado(Integer idSolicitud, String nuevoEstado) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Validar estados permitidos
        if (!nuevoEstado.equals("PGD") && !nuevoEstado.equals("CAN") && !nuevoEstado.equals("PDP")) {
            throw new RuntimeException("Estado no válido. Solo se permite PGD, PDP o CAN");
        }

        EstadoSolicitudEnum estadoNuevo = EstadoSolicitudEnum.valueOf(nuevoEstado);
        solicitud.setEstadoSolicitud(estadoNuevo);
        solicitudRepository.save(solicitud);

        // Notificar al cliente si la solicitud fue marcada como pagada
        if (estadoNuevo == EstadoSolicitudEnum.PGD) {
            SolicitudDTO solicitudDTO = mapToDTO(solicitud);
            whatsAppService.notificarSolicitudPagada(solicitudDTO);
        }
    }

    @Override
    public List<SolicitudDTO> listarPorPedido(Integer idPedido) {
        return solicitudRepository.findByPedido_IdPedido(idPedido)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<SolicitudDTO> listarPorPedidoYEstado(Integer idPedido, String estado) {
        EstadoSolicitudEnum estadoEnum = EstadoSolicitudEnum.valueOf(estado);
        return solicitudRepository.findByPedido_IdPedidoAndEstadoSolicitud(idPedido, estadoEnum)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private SolicitudDTO mapToDTO(Solicitud solicitud) {
        SolicitudDTO dto = new SolicitudDTO();
        dto.setIdSolicitud(solicitud.getIdSolicitud());
        dto.setIdCliente(solicitud.getCliente().getIdUsuario());
        dto.setIdPedido(solicitud.getPedido().getIdPedido());
        dto.setDireccionEntrega(solicitud.getDireccionEntrega());
        dto.setEstadoSolicitud(solicitud.getEstadoSolicitud().name());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        // Incluir fechaLimitePago desde la fecha_cierre del pedido
        dto.setFechaLimitePago(solicitud.getPedido().getFechaCierre());
        // Incluir modificaciones restantes a nivel de solicitud
        dto.setModificacionesRestantes(solicitud.getModificacionesRestantes());
        dto.setProductos(solicitud.getProductos().stream().map(sp -> {
            SolicitudProductoDTO spDTO = new SolicitudProductoDTO();
            spDTO.setIdProducto(sp.getProducto().getIdProducto());
            spDTO.setCantidadSolicitada(sp.getCantidadSolicitada());
            // Calcular precio total: cantidad * precio unitario
            spDTO.setPrecio(sp.getPrecio().multiply(java.math.BigDecimal.valueOf(sp.getCantidadSolicitada())));
            return spDTO;
        }).collect(Collectors.toList()));
        return dto;
    }
}
