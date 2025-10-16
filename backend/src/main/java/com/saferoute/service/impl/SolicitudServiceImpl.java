package com.saferoute.service.impl;

import com.saferoute.dto.*;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.interfaces.ISolicitudService;
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

    public SolicitudServiceImpl(SolicitudRepository solicitudRepository, UsuarioRepository usuarioRepository,
            PedidoRepository pedidoRepository, ProductoRepository productoRepository, RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository) {
        this.usuarioRolRepository = usuarioRolRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.rolRepository = rolRepository;
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
            sp.setModificacionesRestantes(3); // Inicializar con 3 modificaciones
            solicitud.getProductos().add(sp);
        }

        solicitudRepository.save(solicitud);
        return mapToDTO(solicitud);
    }

    @Override
    public SolicitudDTO crearSolicitudClienteNuevo(SolicitudClienteDTO dto) {
        Usuario cliente = null;

        // Buscar usuario existente por correo, teléfono o cédula
        if (usuarioRepository.findByCorreo(dto.getCorreo()).isPresent()) {
            cliente = usuarioRepository.findByCorreo(dto.getCorreo()).get();
        } else if (usuarioRepository.findByTelefono(dto.getTelefono()).isPresent()) {
            cliente = usuarioRepository.findByTelefono(dto.getTelefono()).get();
        } else if (usuarioRepository.findByCedula(dto.getCedula()).isPresent()) {
            cliente = usuarioRepository.findByCedula(dto.getCedula()).get();
        }

        // Si no existe, crear nuevo usuario
        if (cliente == null) {
            Usuario nuevoCliente = new Usuario();
            nuevoCliente.setNombres(dto.getNombres());
            nuevoCliente.setApellidos(dto.getApellidos());
            nuevoCliente.setCorreo(dto.getCorreo());
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

                if (sp.getModificacionesRestantes() <= 0) {
                    throw new RuntimeException("Se alcanzó el número máximo de modificaciones para el producto "
                            + sp.getProducto().getNombreProducto());
                }

                sp.setCantidadSolicitada(prodMod.getCantidadSolicitada());
                sp.setModificacionesRestantes(sp.getModificacionesRestantes() - 1);
            }
        }
        // Compatibilidad con formato antiguo (un solo producto)
        else if (datos.getNuevaCantidad() != null && !solicitud.getProductos().isEmpty()) {
            SolicitudProducto sp = solicitud.getProductos().iterator().next();
            if (sp.getModificacionesRestantes() <= 0)
                throw new RuntimeException("Se alcanzó el número máximo de modificaciones para este producto");
            sp.setCantidadSolicitada(datos.getNuevaCantidad());
            sp.setModificacionesRestantes(sp.getModificacionesRestantes() - 1);
        }

        solicitudRepository.save(solicitud);
        return mapToDTO(solicitud);
    }

    @Override
    public void cancelarSolicitud(Integer idSolicitud) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        solicitud.setEstadoSolicitud(EstadoSolicitudEnum.CAN);
        solicitudRepository.save(solicitud);
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
    public void cambiarEstado(Integer idSolicitud, String nuevoEstado) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Validar estados permitidos
        if (!nuevoEstado.equals("PGD") && !nuevoEstado.equals("CAN") && !nuevoEstado.equals("PDP")) {
            throw new RuntimeException("Estado no válido. Solo se permite PGD, PDP o CAN");
        }

        solicitud.setEstadoSolicitud(EstadoSolicitudEnum.valueOf(nuevoEstado));
        solicitudRepository.save(solicitud);
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
        dto.setProductos(solicitud.getProductos().stream().map(sp -> {
            SolicitudProductoDTO spDTO = new SolicitudProductoDTO();
            spDTO.setIdProducto(sp.getProducto().getIdProducto());
            spDTO.setCantidadSolicitada(sp.getCantidadSolicitada());
            // Calcular precio total: cantidad * precio unitario
            spDTO.setPrecio(sp.getPrecio().multiply(java.math.BigDecimal.valueOf(sp.getCantidadSolicitada())));
            spDTO.setModificacionesRestantes(sp.getModificacionesRestantes());
            return spDTO;
        }).collect(Collectors.toList()));
        return dto;
    }
}
