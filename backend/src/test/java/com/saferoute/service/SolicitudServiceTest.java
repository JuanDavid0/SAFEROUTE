package com.saferoute.service;

import com.saferoute.constants.SolicitudConstants;
import com.saferoute.dto.*;
import com.saferoute.exception.SolicitudBusinessException;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.helper.SolicitudProductoHelper;
import com.saferoute.service.impl.SolicitudServiceImpl;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.service.interfaces.IPedidoService;
import com.saferoute.service.interfaces.IWhatsAppService;
import com.saferoute.service.validator.SolicitudValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests de Solicitud Service
 * 
 * Cobertura:
 * - Crear solicitud (cliente existente y nuevo)
 * - Modificar solicitud
 * - Gestión de productos (agregar, eliminar, modificar cantidad)
 * - Cancelar solicitud
 * - Cancelar solicitudes vencidas
 * - Listar solicitudes (por cliente, cédula, pedido)
 * - Cambiar estado
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Solicitud Service")
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRolRepository usuarioRolRepository;

    @Mock
    private ProductoPedidoRepository productoPedidoRepository;

    @Mock
    private ILogService logService;

    @Mock
    private IWhatsAppService whatsAppService;

    @Mock
    private IPedidoService pedidoService;

    @Mock
    private SolicitudValidator validator;

    @Mock
    private SolicitudProductoHelper helper;

    @InjectMocks
    private SolicitudServiceImpl solicitudService;

    private Usuario cliente;
    private Pedido pedido;
    private Producto producto;
    private Solicitud solicitud;
    private SolicitudDTO solicitudDTO;
    private SolicitudProducto solicitudProducto;
    private ProductoPedido productoPedido;

    @BeforeEach
    void setUp() {
        // Cliente
        cliente = new Usuario();
        cliente.setIdUsuario(1);
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setCedula("1234567890");
        cliente.setTelefono("3001234567");

        // Pedido activo
        pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setEstadoPedido(EstadoPedidoEnum.ACT);
        pedido.setFechaCierre(LocalDate.now().plusDays(7));

        // Producto
        producto = new Producto();
        producto.setIdProducto(1);
        producto.setNombreProducto("Producto Test");
        producto.setPrecioUnitario(new BigDecimal("100.00"));

        // ProductoPedido con límites de cantidad
        productoPedido = new ProductoPedido();
        productoPedido.setProducto(producto);
        productoPedido.setCantidadMin(5);
        productoPedido.setCantidadMax(50);

        // SolicitudProducto
        solicitudProducto = new SolicitudProducto();
        solicitudProducto.setProducto(producto);
        solicitudProducto.setCantidadSolicitada(10);
        solicitudProducto.setPrecio(new BigDecimal("100.00"));

        // Solicitud
        solicitud = new Solicitud();
        solicitud.setIdSolicitud(1);
        solicitud.setCliente(cliente);
        solicitud.setPedido(pedido);
        solicitud.setDireccionEntrega("Calle 123");
        solicitud.setEstadoSolicitud(EstadoSolicitudEnum.PDP);
        solicitud.setFechaSolicitud(LocalDate.now());
        solicitud.setModificacionesRestantes(3);
        solicitud.setProductos(new HashSet<>(Collections.singletonList(solicitudProducto)));

        // SolicitudDTO
        SolicitudProductoDTO spDTO = new SolicitudProductoDTO();
        spDTO.setIdProducto(1);
        spDTO.setCantidadSolicitada(10);

        solicitudDTO = new SolicitudDTO();
        solicitudDTO.setIdPedido(1);
        solicitudDTO.setDireccionEntrega("Calle 123");
        solicitudDTO.setProductos(Collections.singletonList(spDTO));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-01: Crear solicitud exitosamente")
    void testCrearSolicitud_Exitoso() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(productoPedidoRepository.findByPedidoAndProducto(1, 1)).thenReturn(Optional.of(productoPedido));
        doNothing().when(validator).validarPedidoDisponible(any(Pedido.class));
        when(helper.crearSolicitudProducto(any(Solicitud.class), eq(producto), eq(10)))
                .thenReturn(solicitudProducto);
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> {
            Solicitud s = inv.getArgument(0);
            s.setIdSolicitud(1);
            return s;
        });

        // Act
        SolicitudDTO resultado = solicitudService.crearSolicitud(solicitudDTO, 1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdSolicitud()).isEqualTo(1);
        assertThat(resultado.getIdCliente()).isEqualTo(1);
        assertThat(resultado.getIdPedido()).isEqualTo(1);
        
        verify(solicitudRepository).save(any(Solicitud.class));
        verify(logService).registrarLog(eq(1), contains("Solicitud creada"));
        verify(whatsAppService).enviarResumenSolicitud(any(SolicitudDTO.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-02: Crear solicitud con cliente no encontrado")
    void testCrearSolicitud_ClienteNoEncontrado() {
        // Arrange
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> solicitudService.crearSolicitud(solicitudDTO, 999))
                .isInstanceOf(SolicitudBusinessException.class)
                .hasMessage(SolicitudConstants.ERROR_CLIENTE_NO_ENCONTRADO);

        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-03: Crear solicitud con pedido no encontrado")
    void testCrearSolicitud_PedidoNoEncontrado() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.findById(999)).thenReturn(Optional.empty());

        solicitudDTO.setIdPedido(999);

        // Act & Assert
        assertThatThrownBy(() -> solicitudService.crearSolicitud(solicitudDTO, 1))
                .isInstanceOf(SolicitudBusinessException.class)
                .hasMessage(SolicitudConstants.ERROR_PEDIDO_NO_ENCONTRADO);

        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-04: Crear solicitud para cliente nuevo")
    void testCrearSolicitudClienteNuevo_Exitoso() {
        // Arrange
        SolicitudClienteDTO clienteDTO = new SolicitudClienteDTO();
        clienteDTO.setNombres("María");
        clienteDTO.setApellidos("González");
        clienteDTO.setCedula("9876543210");
        clienteDTO.setTelefono("3009876543");
        clienteDTO.setDireccion("Calle 456");
        clienteDTO.setIdPedido(1);
        clienteDTO.setProductos(solicitudDTO.getProductos());

        Rol rolCli = new Rol();
        rolCli.setTipoRol("CLI");

        when(usuarioRepository.findByCedula("9876543210")).thenReturn(Optional.empty());
        when(usuarioRepository.findByTelefono("3009876543")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(2);
            return u;
        });
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(cliente)); // Mock para crearSolicitud
        when(rolRepository.findByTipoRol("CLI")).thenReturn(Optional.of(rolCli));
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(productoPedidoRepository.findByPedidoAndProducto(1, 1)).thenReturn(Optional.of(productoPedido));
        doNothing().when(validator).validarPedidoDisponible(any(Pedido.class));
        when(helper.crearSolicitudProducto(any(Solicitud.class), eq(producto), eq(10)))
                .thenReturn(solicitudProducto);
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> {
            Solicitud s = inv.getArgument(0);
            s.setIdSolicitud(2);
            return s;
        });

        // Act
        SolicitudDTO resultado = solicitudService.crearSolicitudClienteNuevo(clienteDTO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdSolicitud()).isEqualTo(2);
        
        verify(usuarioRepository).save(any(Usuario.class));
        verify(usuarioRolRepository).save(any(UsuarioRol.class));
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-05: Crear solicitud para cliente existente por cédula")
    void testCrearSolicitudClienteNuevo_ClienteExistentePorCedula() {
        // Arrange
        SolicitudClienteDTO clienteDTO = new SolicitudClienteDTO();
        clienteDTO.setCedula("1234567890");
        clienteDTO.setTelefono("3001111111");
        clienteDTO.setIdPedido(1);
        clienteDTO.setProductos(solicitudDTO.getProductos());

        when(usuarioRepository.findByCedula("1234567890")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(cliente)); // Mock para crearSolicitud
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(productoPedidoRepository.findByPedidoAndProducto(1, 1)).thenReturn(Optional.of(productoPedido));
        doNothing().when(validator).validarPedidoDisponible(any(Pedido.class));
        when(helper.crearSolicitudProducto(any(Solicitud.class), eq(producto), eq(10)))
                .thenReturn(solicitudProducto);
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> {
            Solicitud s = inv.getArgument(0);
            s.setIdSolicitud(1);
            return s;
        });

        // Act
        SolicitudDTO resultado = solicitudService.crearSolicitudClienteNuevo(clienteDTO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(usuarioRepository, never()).save(any(Usuario.class)); // No crea nuevo usuario
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-06: Modificar solicitud exitosamente")
    void testModificarSolicitud_Exitoso() {
        // Arrange
        SolicitudModificacionDTO modificacion = new SolicitudModificacionDTO();
        modificacion.setDireccionEntrega("Nueva Calle 789");

        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        doNothing().when(validator).validarSolicitudModificable(any(Solicitud.class));
        doNothing().when(helper).decrementarModificaciones(any(Solicitud.class));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SolicitudDTO resultado = solicitudService.modificarSolicitud(1, modificacion);

        // Assert
        assertThat(resultado).isNotNull();
        verify(validator).validarSolicitudModificable(any(Solicitud.class));
        verify(helper).decrementarModificaciones(any(Solicitud.class));
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-07: Agregar producto a solicitud exitosamente")
    void testAgregarProducto_Exitoso() {
        // Arrange
        Producto nuevoProducto = new Producto();
        nuevoProducto.setIdProducto(2);
        nuevoProducto.setNombreProducto("Nuevo Producto");
        nuevoProducto.setPrecioUnitario(new BigDecimal("200.00"));

        ProductoPedido productoPedido2 = new ProductoPedido();
        productoPedido2.setProducto(nuevoProducto);
        productoPedido2.setCantidadMin(1);
        productoPedido2.setCantidadMax(10);

        SolicitudProducto nuevoSP = new SolicitudProducto();
        nuevoSP.setProducto(nuevoProducto);
        nuevoSP.setCantidadSolicitada(5);
        nuevoSP.setPrecio(new BigDecimal("200.00"));

        SolicitudProductoDTO productoDTO = new SolicitudProductoDTO();
        productoDTO.setIdProducto(2);
        productoDTO.setCantidadSolicitada(5);

        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        doNothing().when(validator).validarSolicitudModificable(any(Solicitud.class));
        when(productoRepository.findById(2)).thenReturn(Optional.of(nuevoProducto));
        when(productoPedidoRepository.findByPedidoAndProducto(1, 2)).thenReturn(Optional.of(productoPedido2));
        when(helper.productoExisteEnPedido(any(Solicitud.class), eq(2))).thenReturn(true);
        when(helper.productoExisteEnSolicitud(any(Solicitud.class), eq(2))).thenReturn(false);
        when(helper.crearSolicitudProducto(any(Solicitud.class), eq(nuevoProducto), eq(5)))
                .thenReturn(nuevoSP);
        doNothing().when(helper).decrementarModificaciones(any(Solicitud.class));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SolicitudDTO resultado = solicitudService.agregarProducto(1, productoDTO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(validator).validarSolicitudModificable(any(Solicitud.class));
        verify(logService).registrarLog(eq(1), contains("Producto agregado"));
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-08: Agregar producto que no está en el pedido")
    void testAgregarProducto_ProductoNoEnPedido() {
        // Arrange
        SolicitudProductoDTO productoDTO = new SolicitudProductoDTO();
        productoDTO.setIdProducto(99);
        productoDTO.setCantidadSolicitada(5);

        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        doNothing().when(validator).validarSolicitudModificable(any(Solicitud.class));
        when(productoRepository.findById(99)).thenReturn(Optional.of(producto));
        when(helper.productoExisteEnPedido(any(Solicitud.class), eq(99))).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> solicitudService.agregarProducto(1, productoDTO))
                .isInstanceOf(SolicitudBusinessException.class)
                .hasMessage(SolicitudConstants.ERROR_PRODUCTO_NO_EN_PEDIDO);

        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-09: Agregar producto que ya está en la solicitud")
    void testAgregarProducto_ProductoYaEnSolicitud() {
        // Arrange
        SolicitudProductoDTO productoDTO = new SolicitudProductoDTO();
        productoDTO.setIdProducto(1);
        productoDTO.setCantidadSolicitada(5);

        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        doNothing().when(validator).validarSolicitudModificable(any(Solicitud.class));
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(helper.productoExisteEnPedido(any(Solicitud.class), eq(1))).thenReturn(true);
        when(helper.productoExisteEnSolicitud(any(Solicitud.class), eq(1))).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> solicitudService.agregarProducto(1, productoDTO))
                .isInstanceOf(SolicitudBusinessException.class)
                .hasMessage(SolicitudConstants.ERROR_PRODUCTO_YA_EN_SOLICITUD);

        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-10: Eliminar producto de solicitud exitosamente")
    void testEliminarProducto_Exitoso() {
        // Arrange
        // Agregar un segundo producto para poder eliminar uno
        SolicitudProducto sp2 = new SolicitudProducto();
        sp2.setProducto(producto);
        sp2.setCantidadSolicitada(5);
        sp2.setPrecio(new BigDecimal("100.00")); // Establecer precio
        solicitud.getProductos().add(sp2);

        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        doNothing().when(validator).validarSolicitudModificable(any(Solicitud.class));
        doNothing().when(validator).validarCantidadProductosParaEliminar(any(Solicitud.class));
        when(helper.buscarProductoEnSolicitud(any(Solicitud.class), eq(1)))
                .thenReturn(solicitudProducto);
        doNothing().when(helper).decrementarModificaciones(any(Solicitud.class));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SolicitudDTO resultado = solicitudService.eliminarProducto(1, 1);

        // Assert
        assertThat(resultado).isNotNull();
        verify(validator).validarCantidadProductosParaEliminar(any(Solicitud.class));
        verify(logService).registrarLog(eq(1), contains("Producto eliminado"));
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-11: Modificar cantidad de producto exitosamente")
    void testModificarCantidadProducto_Exitoso() {
        // Arrange
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        doNothing().when(validator).validarSolicitudModificable(any(Solicitud.class));
        when(helper.buscarProductoEnSolicitud(any(Solicitud.class), eq(1)))
                .thenReturn(solicitudProducto);
        when(productoPedidoRepository.findByPedidoAndProducto(1, 1)).thenReturn(Optional.of(productoPedido));
        doNothing().when(helper).decrementarModificaciones(any(Solicitud.class));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SolicitudDTO resultado = solicitudService.modificarCantidadProducto(1, 1, 20);

        // Assert
        assertThat(resultado).isNotNull();
        verify(validator).validarSolicitudModificable(any(Solicitud.class));
        verify(helper).decrementarModificaciones(any(Solicitud.class));
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-12: Modificar cantidad menor al mínimo lanza excepción")
    void testModificarCantidadProducto_CantidadMenorAlMinimo() {
        // Arrange
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        doNothing().when(validator).validarSolicitudModificable(any(Solicitud.class));
        when(helper.buscarProductoEnSolicitud(any(Solicitud.class), eq(1)))
                .thenReturn(solicitudProducto);
        when(productoPedidoRepository.findByPedidoAndProducto(1, 1)).thenReturn(Optional.of(productoPedido));

        // Act & Assert (cantidad 2 < mínimo 5)
        assertThatThrownBy(() -> solicitudService.modificarCantidadProducto(1, 1, 2))
                .isInstanceOf(SolicitudBusinessException.class)
                .hasMessageContaining("menor a la cantidad mínima");

        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-13: Modificar cantidad mayor al máximo lanza excepción")
    void testModificarCantidadProducto_CantidadMayorAlMaximo() {
        // Arrange
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        doNothing().when(validator).validarSolicitudModificable(any(Solicitud.class));
        when(helper.buscarProductoEnSolicitud(any(Solicitud.class), eq(1)))
                .thenReturn(solicitudProducto);
        when(productoPedidoRepository.findByPedidoAndProducto(1, 1)).thenReturn(Optional.of(productoPedido));

        // Act & Assert (cantidad 100 > máximo 50)
        assertThatThrownBy(() -> solicitudService.modificarCantidadProducto(1, 1, 100))
                .isInstanceOf(SolicitudBusinessException.class)
                .hasMessageContaining("excede la cantidad máxima");

        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-14: Cancelar solicitud exitosamente")
    void testCancelarSolicitud_Exitoso() {
        // Arrange
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        doNothing().when(validator).validarSolicitudEstadoPDP(any(Solicitud.class));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        solicitudService.cancelarSolicitud(1);

        // Assert
        verify(validator).validarSolicitudEstadoPDP(any(Solicitud.class));
        verify(solicitudRepository).save(argThat(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.CAN));
        verify(logService).registrarLog(eq(1), contains("Solicitud cancelada"));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-15: Cancelar solicitudes vencidas")
    void testCancelarSolicitudesVencidas_Exitoso() {
        // Arrange
        Solicitud solicitud2 = new Solicitud();
        solicitud2.setIdSolicitud(2);
        solicitud2.setEstadoSolicitud(EstadoSolicitudEnum.PDP);

        List<Solicitud> vencidas = Arrays.asList(solicitud, solicitud2);

        when(solicitudRepository.findByPedidoFechaCierreBeforeAndEstadoSolicitud(
                any(LocalDate.class), eq(EstadoSolicitudEnum.PDP)))
                .thenReturn(vencidas);
        when(solicitudRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        solicitudService.cancelarSolicitudesVencidas();

        // Assert
        verify(solicitudRepository).saveAll(anyList());
        // Verificar que ambas solicitudes fueron actualizadas a CAN
        assertThat(solicitud.getEstadoSolicitud()).isEqualTo(EstadoSolicitudEnum.CAN);
        assertThat(solicitud2.getEstadoSolicitud()).isEqualTo(EstadoSolicitudEnum.CAN);
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-16: Listar solicitudes por cliente")
    void testListarSolicitudesCliente_Exitoso() {
        // Arrange
        Solicitud solicitud2 = new Solicitud();
        solicitud2.setIdSolicitud(2);
        solicitud2.setCliente(cliente);
        solicitud2.setPedido(pedido);
        solicitud2.setDireccionEntrega("Otra Calle");
        solicitud2.setEstadoSolicitud(EstadoSolicitudEnum.PGD);
        solicitud2.setProductos(new HashSet<>());

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(solicitudRepository.findByCliente(cliente))
                .thenReturn(Arrays.asList(solicitud, solicitud2));

        // Act
        List<SolicitudDTO> resultado = solicitudService.listarSolicitudesCliente(1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getIdSolicitud()).isEqualTo(1);
        assertThat(resultado.get(1).getIdSolicitud()).isEqualTo(2);
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-17: Listar solicitudes por cédula con ordenamiento")
    void testListarSolicitudesPorCedula_ConOrdenamiento() {
        // Arrange
        Solicitud solicitudPGD = new Solicitud();
        solicitudPGD.setIdSolicitud(2);
        solicitudPGD.setCliente(cliente);
        solicitudPGD.setPedido(pedido);
        solicitudPGD.setEstadoSolicitud(EstadoSolicitudEnum.PGD);
        solicitudPGD.setProductos(new HashSet<>());

        Solicitud solicitudCAN = new Solicitud();
        solicitudCAN.setIdSolicitud(3);
        solicitudCAN.setCliente(cliente);
        solicitudCAN.setPedido(pedido);
        solicitudCAN.setEstadoSolicitud(EstadoSolicitudEnum.CAN);
        solicitudCAN.setProductos(new HashSet<>());

        // Retornar en orden: CAN, PGD, PDP (debe ordenarse a PDP, PGD, CAN)
        when(solicitudRepository.findByCliente_Cedula("1234567890"))
                .thenReturn(Arrays.asList(solicitudCAN, solicitudPGD, solicitud));

        // Act
        List<SolicitudDTO> resultado = solicitudService.listarSolicitudesPorCedula("1234567890");

        // Assert
        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0).getEstadoSolicitud()).isEqualTo("PDP"); // Prioridad 1
        assertThat(resultado.get(1).getEstadoSolicitud()).isEqualTo("PGD"); // Prioridad 2
        assertThat(resultado.get(2).getEstadoSolicitud()).isEqualTo("CAN"); // Prioridad 3
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-18: Listar solicitudes por cédula y pedido")
    void testListarSolicitudesPorCedulaYPedido_Exitoso() {
        // Arrange
        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setIdPedido(1);

        when(pedidoService.obtenerPedidoPorHash("abc123")).thenReturn(pedidoDTO);
        when(solicitudRepository.findByCliente_Cedula("1234567890"))
                .thenReturn(Collections.singletonList(solicitud));

        // Act
        List<SolicitudDTO> resultado = solicitudService.listarSolicitudesPorCedulaYPedido("1234567890", "abc123");

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdPedido()).isEqualTo(1);
        verify(pedidoService).obtenerPedidoPorHash("abc123");
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-19: Cambiar estado a PGD y notificar")
    void testCambiarEstado_APagado_Notifica() {
        // Arrange
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        solicitudService.cambiarEstado(1, "PGD");

        // Assert
        verify(solicitudRepository).save(argThat(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.PGD));
        verify(whatsAppService).notificarSolicitudPagada(any(SolicitudDTO.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-20: Cambiar estado a valor no válido lanza excepción")
    void testCambiarEstado_EstadoNoValido() {
        // Arrange
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThatThrownBy(() -> solicitudService.cambiarEstado(1, "INVALIDO"))
                .isInstanceOf(SolicitudBusinessException.class)
                .hasMessage(SolicitudConstants.ERROR_ESTADO_NO_VALIDO);

        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-21: Listar solicitudes por pedido")
    void testListarPorPedido_Exitoso() {
        // Arrange
        when(solicitudRepository.findByPedido_IdPedido(1))
                .thenReturn(Collections.singletonList(solicitud));

        // Act
        List<SolicitudDTO> resultado = solicitudService.listarPorPedido(1);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdPedido()).isEqualTo(1);
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-22: Listar solicitudes por pedido y estado")
    void testListarPorPedidoYEstado_Exitoso() {
        // Arrange
        when(solicitudRepository.findByPedido_IdPedidoAndEstadoSolicitud(1, EstadoSolicitudEnum.PDP))
                .thenReturn(Collections.singletonList(solicitud));

        // Act
        List<SolicitudDTO> resultado = solicitudService.listarPorPedidoYEstado(1, "PDP");

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEstadoSolicitud()).isEqualTo("PDP");
    }

    @Test
    @DisplayName("TC-UNIT-SOLIC-23: Solicitud no encontrada al modificar")
    void testModificarSolicitud_SolicitudNoEncontrada() {
        // Arrange
        when(solicitudRepository.findById(999)).thenReturn(Optional.empty());

        SolicitudModificacionDTO modificacion = new SolicitudModificacionDTO();

        // Act & Assert
        assertThatThrownBy(() -> solicitudService.modificarSolicitud(999, modificacion))
                .isInstanceOf(SolicitudBusinessException.class)
                .hasMessage(SolicitudConstants.ERROR_SOLICITUD_NO_ENCONTRADA);
    }
}
