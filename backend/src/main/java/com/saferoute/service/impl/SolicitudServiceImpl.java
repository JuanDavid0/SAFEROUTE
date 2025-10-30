package com.saferoute.service.impl;

import com.saferoute.constants.SolicitudConstants;
import com.saferoute.dto.*;
import com.saferoute.exception.SolicitudBusinessException;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.helper.SolicitudProductoHelper;
import com.saferoute.service.interfaces.ISolicitudService;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.service.interfaces.IWhatsAppService;
import com.saferoute.service.interfaces.IPedidoService;
import com.saferoute.service.validator.SolicitudValidator;
import com.saferoute.repository.UsuarioRolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de Solicitudes
 * Refactorizado siguiendo principios SOLID y buenas prácticas
 */
@Service
@Transactional
public class SolicitudServiceImpl implements ISolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final ILogService logService;
    private final IWhatsAppService whatsAppService;
    private final IPedidoService pedidoService;
    private final SolicitudValidator validator;
    private final SolicitudProductoHelper helper;

    public SolicitudServiceImpl(
            SolicitudRepository solicitudRepository,
            UsuarioRepository usuarioRepository,
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            ILogService logService,
            IWhatsAppService whatsAppService,
            ProductoPedidoRepository productoPedidoRepository,
            IPedidoService pedidoService,
            SolicitudValidator validator,
            SolicitudProductoHelper helper) {
        this.usuarioRolRepository = usuarioRolRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.rolRepository = rolRepository;
        this.logService = logService;
        this.whatsAppService = whatsAppService;
        this.pedidoService = pedidoService;
        this.validator = validator;
        this.helper = helper;
    }

    @Override
    public SolicitudDTO crearSolicitud(SolicitudDTO dto, Integer idCliente) {
        Usuario cliente = usuarioRepository.findById(idCliente)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_CLIENTE_NO_ENCONTRADO));

        Pedido pedido = pedidoRepository.findById(dto.getIdPedido())
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_PEDIDO_NO_ENCONTRADO));

        validator.validarPedidoDisponible(pedido);

        Solicitud solicitud = construirNuevaSolicitud(cliente, pedido, dto.getDireccionEntrega());
        agregarProductosASolicitud(solicitud, dto.getProductos(), pedido.getIdPedido());

        solicitudRepository.save(solicitud);

        registrarCreacionSolicitud(idCliente, solicitud, dto.getProductos().size());

        SolicitudDTO solicitudDTO = mapToDTO(solicitud);
        whatsAppService.enviarResumenSolicitud(solicitudDTO);

        return solicitudDTO;
    }

    private Solicitud construirNuevaSolicitud(Usuario cliente, Pedido pedido, String direccionEntrega) {
        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cliente);
        solicitud.setPedido(pedido);
        solicitud.setDireccionEntrega(direccionEntrega);
        solicitud.setEstadoSolicitud(EstadoSolicitudEnum.PDP);
        return solicitud;
    }

    private void agregarProductosASolicitud(Solicitud solicitud, List<SolicitudProductoDTO> productosDTO,
            Integer idPedido) {
        for (SolicitudProductoDTO spDTO : productosDTO) {
            Producto producto = productoRepository.findById(spDTO.getIdProducto())
                    .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_PRODUCTO_NO_ENCONTRADO));

            validarCantidadProducto(idPedido, spDTO.getIdProducto(), spDTO.getCantidadSolicitada());

            SolicitudProducto sp = helper.crearSolicitudProducto(
                    solicitud,
                    producto,
                    spDTO.getCantidadSolicitada());

            sp.setPrecio(producto.getPrecioUnitario());
            solicitud.getProductos().add(sp);
        }
    }

    private void registrarCreacionSolicitud(Integer idCliente, Solicitud solicitud, int cantidadProductos) {
        logService.registrarLog(idCliente, String.format(
                SolicitudConstants.LOG_SOLICITUD_CREADA,
                solicitud.getIdSolicitud(),
                solicitud.getPedido().getIdPedido(),
                cantidadProductos));
    }

    @Override
    public SolicitudDTO crearSolicitudClienteNuevo(SolicitudClienteDTO dto) {
        Usuario cliente = buscarOCrearCliente(dto);

        SolicitudDTO solicitudDTO = new SolicitudDTO();
        solicitudDTO.setIdPedido(dto.getIdPedido());
        solicitudDTO.setDireccionEntrega(dto.getDireccion());
        solicitudDTO.setProductos(dto.getProductos());

        return crearSolicitud(solicitudDTO, cliente.getIdUsuario());
    }

    private Usuario buscarOCrearCliente(SolicitudClienteDTO dto) {
        Usuario cliente = buscarClienteExistente(dto);
        if (cliente == null) {
            cliente = crearNuevoCliente(dto);
        }
        return cliente;
    }

    private Usuario buscarClienteExistente(SolicitudClienteDTO dto) {
        return usuarioRepository.findByCedula(dto.getCedula())
                .or(() -> usuarioRepository.findByTelefono(dto.getTelefono()))
                .orElse(null);
    }

    private Usuario crearNuevoCliente(SolicitudClienteDTO dto) {
        Usuario nuevoCliente = new Usuario();
        nuevoCliente.setNombres(dto.getNombres());
        nuevoCliente.setApellidos(dto.getApellidos());
        nuevoCliente.setTelefono(dto.getTelefono());
        nuevoCliente.setCedula(dto.getCedula());
        nuevoCliente.setDireccion(dto.getDireccion());
        Usuario cliente = usuarioRepository.save(nuevoCliente);

        asignarRolCliente(cliente);
        return cliente;
    }

    private void asignarRolCliente(Usuario cliente) {
        Rol rolCliente = rolRepository.findByTipoRol(SolicitudConstants.ROL_CLIENTE)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_ROL_CLI_NO_ENCONTRADO));
        UsuarioRol usuarioRol = new UsuarioRol(rolCliente, cliente);
        usuarioRolRepository.save(usuarioRol);
    }

    @Override
    public SolicitudDTO modificarSolicitud(Integer idSolicitud, SolicitudModificacionDTO datos) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_SOLICITUD_NO_ENCONTRADA));

        validator.validarSolicitudModificable(solicitud);

        modificarDatosBasicos(solicitud, datos);
        modificarProductosLista(solicitud, datos);
        modificarProductoCompatibilidad(solicitud, datos);

        helper.decrementarModificaciones(solicitud);
        solicitudRepository.save(solicitud);

        return mapToDTO(solicitud);
    }

    private void modificarDatosBasicos(Solicitud solicitud, SolicitudModificacionDTO datos) {
        if (datos.getDireccionEntrega() != null) {
            solicitud.setDireccionEntrega(datos.getDireccionEntrega());
        }
    }

    private void modificarProductosLista(Solicitud solicitud, SolicitudModificacionDTO datos) {
        if (datos.getProductos() != null && !datos.getProductos().isEmpty()) {
            for (SolicitudModificacionDTO.ProductoModificacionDTO prodMod : datos.getProductos()) {
                SolicitudProducto sp = helper.buscarProductoEnSolicitud(solicitud, prodMod.getIdProducto());
                sp.setCantidadSolicitada(prodMod.getCantidadSolicitada());
            }
        }
    }

    private void modificarProductoCompatibilidad(Solicitud solicitud, SolicitudModificacionDTO datos) {
        // Compatibilidad con formato antiguo (un solo producto)
        if (datos.getNuevaCantidad() != null && !solicitud.getProductos().isEmpty()) {
            SolicitudProducto sp = solicitud.getProductos().iterator().next();
            sp.setCantidadSolicitada(datos.getNuevaCantidad());
        }
    }

    @Override
    public SolicitudDTO agregarProducto(Integer idSolicitud, SolicitudProductoDTO productoDTO) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_SOLICITUD_NO_ENCONTRADA));

        validator.validarSolicitudModificable(solicitud);

        Producto producto = productoRepository.findById(productoDTO.getIdProducto())
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_PRODUCTO_NO_ENCONTRADO));

        validarProductoParaAgregar(solicitud, productoDTO.getIdProducto());
        validarCantidadProducto(solicitud.getPedido().getIdPedido(), productoDTO.getIdProducto(),
                productoDTO.getCantidadSolicitada());

        agregarProducto(solicitud, producto, productoDTO.getCantidadSolicitada());
        helper.decrementarModificaciones(solicitud);

        solicitudRepository.save(solicitud);

        registrarAgregarProducto(solicitud, producto, productoDTO.getCantidadSolicitada());

        return mapToDTO(solicitud);
    }

    private void validarProductoParaAgregar(Solicitud solicitud, Integer idProducto) {
        if (!helper.productoExisteEnPedido(solicitud, idProducto)) {
            throw new SolicitudBusinessException(SolicitudConstants.ERROR_PRODUCTO_NO_EN_PEDIDO);
        }
        if (helper.productoExisteEnSolicitud(solicitud, idProducto)) {
            throw new SolicitudBusinessException(SolicitudConstants.ERROR_PRODUCTO_YA_EN_SOLICITUD);
        }
    }

    private void agregarProducto(Solicitud solicitud, Producto producto, Integer cantidad) {
        SolicitudProducto sp = helper.crearSolicitudProducto(solicitud, producto, cantidad);
        sp.setPrecio(producto.getPrecioUnitario());
        solicitud.getProductos().add(sp);
    }

    private void registrarAgregarProducto(Solicitud solicitud, Producto producto, Integer cantidad) {
        logService.registrarLog(solicitud.getCliente().getIdUsuario(), String.format(
                SolicitudConstants.LOG_PRODUCTO_AGREGADO,
                solicitud.getIdSolicitud(),
                producto.getNombreProducto(),
                cantidad,
                solicitud.getModificacionesRestantes()));
    }

    @Override
    public SolicitudDTO eliminarProducto(Integer idSolicitud, Integer idProducto) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_SOLICITUD_NO_ENCONTRADA));

        validator.validarSolicitudModificable(solicitud);
        validator.validarCantidadProductosParaEliminar(solicitud);

        SolicitudProducto sp = helper.buscarProductoEnSolicitud(solicitud, idProducto);
        String nombreProducto = sp.getProducto().getNombreProducto();

        solicitud.getProductos().remove(sp);
        helper.decrementarModificaciones(solicitud);

        solicitudRepository.save(solicitud);

        registrarEliminarProducto(solicitud, nombreProducto);

        return mapToDTO(solicitud);
    }

    private void registrarEliminarProducto(Solicitud solicitud, String nombreProducto) {
        logService.registrarLog(solicitud.getCliente().getIdUsuario(), String.format(
                SolicitudConstants.LOG_PRODUCTO_ELIMINADO,
                solicitud.getIdSolicitud(),
                nombreProducto,
                solicitud.getModificacionesRestantes()));
    }

    @Override
    public SolicitudDTO modificarCantidadProducto(Integer idSolicitud, Integer idProducto, Integer nuevaCantidad) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_SOLICITUD_NO_ENCONTRADA));

        validator.validarSolicitudModificable(solicitud);

        SolicitudProducto sp = helper.buscarProductoEnSolicitud(solicitud, idProducto);
        validarCantidadProducto(solicitud.getPedido().getIdPedido(), idProducto, nuevaCantidad);

        sp.setCantidadSolicitada(nuevaCantidad);
        helper.decrementarModificaciones(solicitud);

        solicitudRepository.save(solicitud);

        return mapToDTO(solicitud);
    }

    @Override
    public void cancelarSolicitud(Integer idSolicitud) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_SOLICITUD_NO_ENCONTRADA));

        validator.validarSolicitudEstadoPDP(solicitud);

        solicitud.setEstadoSolicitud(EstadoSolicitudEnum.CAN);
        solicitudRepository.save(solicitud);

        registrarCancelacionSolicitud(solicitud);
    }

    private void registrarCancelacionSolicitud(Solicitud solicitud) {
        Integer clienteId = solicitud.getCliente().getIdUsuario();
        logService.registrarLog(clienteId, String.format(
                SolicitudConstants.LOG_SOLICITUD_CANCELADA,
                solicitud.getIdSolicitud(),
                solicitud.getPedido().getIdPedido()));
    }

    @Override
    public void cancelarSolicitudesVencidas() {
        List<Solicitud> vencidas = solicitudRepository
                .findByPedidoFechaCierreBeforeAndEstadoSolicitud(LocalDate.now(), EstadoSolicitudEnum.PDP);
        vencidas.forEach(s -> s.setEstadoSolicitud(EstadoSolicitudEnum.CAN));
        solicitudRepository.saveAll(vencidas);
    }

    @Override
    public List<SolicitudDTO> listarSolicitudesCliente(Integer idCliente) {
        Usuario cliente = usuarioRepository.findById(idCliente)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_CLIENTE_NO_ENCONTRADO));
        return solicitudRepository.findByCliente(cliente)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudDTO> listarSolicitudesPorCedula(String cedula) {
        List<Solicitud> solicitudes = solicitudRepository.findByCliente_Cedula(cedula);

        return solicitudes.stream()
                .sorted(this::compararPorPrioridad)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudDTO> listarSolicitudesPorCedulaYPedido(String cedula, String hashPedido) {
        PedidoDTO pedidoDTO = pedidoService.obtenerPedidoPorHash(hashPedido);
        List<Solicitud> solicitudes = solicitudRepository.findByCliente_Cedula(cedula);

        return solicitudes.stream()
                .filter(s -> s.getPedido().getIdPedido().equals(pedidoDTO.getIdPedido()))
                .sorted(this::compararPorPrioridad)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private int compararPorPrioridad(Solicitud s1, Solicitud s2) {
        int prioridad1 = obtenerPrioridadEstado(s1.getEstadoSolicitud());
        int prioridad2 = obtenerPrioridadEstado(s2.getEstadoSolicitud());
        return Integer.compare(prioridad1, prioridad2);
    }

    /**
     * Define la prioridad de ordenamiento por estado
     * PDP = 1 (primero), PGD = 2 (segundo), CAN = 3 (último)
     */
    private int obtenerPrioridadEstado(EstadoSolicitudEnum estado) {
        return switch (estado) {
            case PDP -> 1; // Pendiente de Pago - primero
            case PGD -> 2; // Pagado - segundo
            case CAN -> 3; // Cancelado - último
        };
    }

    @Override
    public void cambiarEstado(Integer idSolicitud, String nuevoEstado) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_SOLICITUD_NO_ENCONTRADA));

        validarEstadoPermitido(nuevoEstado);

        EstadoSolicitudEnum estadoNuevo = EstadoSolicitudEnum.valueOf(nuevoEstado);
        solicitud.setEstadoSolicitud(estadoNuevo);
        solicitudRepository.save(solicitud);

        notificarSiPagada(solicitud, estadoNuevo);
    }

    private void validarEstadoPermitido(String nuevoEstado) {
        if (!nuevoEstado.equals(SolicitudConstants.ESTADO_PGD) &&
                !nuevoEstado.equals(SolicitudConstants.ESTADO_CAN) &&
                !nuevoEstado.equals(SolicitudConstants.ESTADO_PDP)) {
            throw new SolicitudBusinessException(SolicitudConstants.ERROR_ESTADO_NO_VALIDO);
        }
    }

    private void notificarSiPagada(Solicitud solicitud, EstadoSolicitudEnum estadoNuevo) {
        if (estadoNuevo == EstadoSolicitudEnum.PGD) {
            SolicitudDTO solicitudDTO = mapToDTO(solicitud);
            whatsAppService.notificarSolicitudPagada(solicitudDTO);
        }
    }

    @Override
    public List<SolicitudDTO> listarPorPedido(Integer idPedido) {
        return solicitudRepository.findByPedido_IdPedido(idPedido)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudDTO> listarPorPedidoYEstado(Integer idPedido, String estado) {
        EstadoSolicitudEnum estadoEnum = EstadoSolicitudEnum.valueOf(estado);
        return solicitudRepository.findByPedido_IdPedidoAndEstadoSolicitud(idPedido, estadoEnum)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private SolicitudDTO mapToDTO(Solicitud solicitud) {
        SolicitudDTO dto = new SolicitudDTO();
        dto.setIdSolicitud(solicitud.getIdSolicitud());
        dto.setIdCliente(solicitud.getCliente().getIdUsuario());

        // Construir nombre completo del cliente de forma segura
        String nombreCompleto = construirNombreCompleto(
                solicitud.getCliente().getNombres(),
                solicitud.getCliente().getApellidos());
        dto.setNombreCliente(nombreCompleto);

        dto.setIdPedido(solicitud.getPedido().getIdPedido());
        dto.setDireccionEntrega(solicitud.getDireccionEntrega());
        dto.setEstadoSolicitud(solicitud.getEstadoSolicitud().name());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setFechaLimitePago(solicitud.getPedido().getFechaCierre());
        dto.setModificacionesRestantes(solicitud.getModificacionesRestantes());
        dto.setProductos(solicitud.getProductos().stream()
                .map(this::mapSolicitudProductoToDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private SolicitudProductoDTO mapSolicitudProductoToDTO(SolicitudProducto sp) {
        SolicitudProductoDTO spDTO = new SolicitudProductoDTO();
        spDTO.setIdProducto(sp.getProducto().getIdProducto());
        spDTO.setCantidadSolicitada(sp.getCantidadSolicitada());
        spDTO.setPrecio(sp.getPrecio().multiply(java.math.BigDecimal.valueOf(sp.getCantidadSolicitada())));
        return spDTO;
    }

    /**
     * Valida que la cantidad solicitada esté dentro de los límites definidos
     * en la tabla PRODUCTO_PEDIDO (cantidad_min y cantidad_max)
     */
    private void validarCantidadProducto(Integer idPedido, Integer idProducto, Integer cantidad) {
        ProductoPedido productoPedido = productoPedidoRepository
                .findByPedidoAndProducto(idPedido, idProducto)
                .orElseThrow(() -> new SolicitudBusinessException(SolicitudConstants.ERROR_PRODUCTO_NO_ENCONTRADO));

        validarLimitesCantidad(cantidad, productoPedido);
    }

    private void validarLimitesCantidad(Integer cantidad, ProductoPedido productoPedido) {
        Integer cantidadMin = productoPedido.getCantidadMin();
        Integer cantidadMax = productoPedido.getCantidadMax();

        if (cantidad < cantidadMin) {
            throw new SolicitudBusinessException(String.format(
                    "La cantidad solicitada (%d) es menor a la cantidad mínima permitida (%d) para este producto",
                    cantidad, cantidadMin));
        }

        if (cantidadMax != null && cantidad > cantidadMax) {
            throw new SolicitudBusinessException(String.format(
                    "La cantidad solicitada (%d) excede la cantidad máxima permitida (%d) para este producto",
                    cantidad, cantidadMax));
        }
    }

    /**
     * Construye el nombre completo del cliente manejando valores null de forma
     * segura
     */
    private String construirNombreCompleto(String nombres, String apellidos) {
        // Manejar null o strings vacíos
        String nombreLimpio = (nombres != null && !nombres.trim().isEmpty()) ? nombres.trim() : "";
        String apellidoLimpio = (apellidos != null && !apellidos.trim().isEmpty()) ? apellidos.trim() : "";

        // Si ambos están vacíos, retornar N/A
        if (nombreLimpio.isEmpty() && apellidoLimpio.isEmpty()) {
            return "N/A";
        }

        // Si solo uno está vacío, retornar el que tiene valor
        if (nombreLimpio.isEmpty()) {
            return apellidoLimpio;
        }

        if (apellidoLimpio.isEmpty()) {
            return nombreLimpio;
        }

        // Ambos tienen valor, concatenar con espacio
        return nombreLimpio + " " + apellidoLimpio;
    }
}
