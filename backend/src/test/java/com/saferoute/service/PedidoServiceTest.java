package com.saferoute.service;

import com.saferoute.constants.PedidoConstants;
import com.saferoute.dto.PedidoDTO;
import com.saferoute.dto.ProductoPedidoDTO;
import com.saferoute.exception.PedidoBusinessException;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.helper.ProductoPedidoHelper;
import com.saferoute.service.impl.PedidoServiceImpl;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.service.interfaces.IWhatsAppService;
import com.saferoute.service.validator.PedidoValidator;
import org.hashids.Hashids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Tests de Pedido Service
 * 
 * Cobertura:
 * - Crear pedido con productos
 * - Actualizar estado de pedido (con validación de transiciones)
 * - Actualizar pedido (creado vs activo)
 * - Agregar/modificar/eliminar productos
 * - Cancelar pedido
 * - Listar y obtener pedidos
 * - Generar y obtener por URL hash
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Pedido Service")
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private ILogService logService;

    @Mock
    private IWhatsAppService whatsAppService;

    @Mock
    private Hashids hashids;

    @Mock
    private PedidoValidator validator;

    @Mock
    private ProductoPedidoHelper helper;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Usuario admin;
    private Pedido pedido;
    private PedidoDTO pedidoDTO;
    private Producto producto;
    private ProductoPedido productoPedido;

    @BeforeEach
    void setUp() {
        // Usuario administrador
        admin = new Usuario();
        admin.setIdUsuario(1);
        admin.setNombres("Admin");
        admin.setApellidos("Test");
        admin.setCedula("1234567890");

        // Producto
        producto = new Producto();
        producto.setIdProducto(1);
        producto.setNombreProducto("Producto Test");
        producto.setPrecioUnitario(new BigDecimal("1000.00"));

        // ProductoPedido
        productoPedido = new ProductoPedido();
        productoPedido.setProducto(producto);
        productoPedido.setCantidadMin(10);
        productoPedido.setCantidadMax(50);

        // Pedido
        pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setAdmin(admin);
        pedido.setEstadoPedido(EstadoPedidoEnum.CRT);
        pedido.setFechaCreado(LocalDate.now());
        pedido.setFechaCierre(LocalDate.now().plusDays(7));
        pedido.setProductos(new HashSet<>(Collections.singletonList(productoPedido)));

        // PedidoDTO
        ProductoPedidoDTO ppDTO = new ProductoPedidoDTO(1, 10, 50);
        pedidoDTO = new PedidoDTO();
        pedidoDTO.setFechaCierre(LocalDate.now().plusDays(7));
        pedidoDTO.setProductos(Collections.singletonList(ppDTO));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-01: Crear pedido exitosamente")
    void testCrearPedido_Exitoso() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(admin));
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(helper.crearProductoPedido(any(Pedido.class), eq(producto), eq(10), eq(50)))
                .thenReturn(productoPedido);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setIdPedido(1);
            return p;
        });

        // Act
        PedidoDTO resultado = pedidoService.crearPedido(pedidoDTO, 1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPedido()).isEqualTo(1);
        assertThat(resultado.getIdAdmin()).isEqualTo(1);
        assertThat(resultado.getEstadoPedido()).isEqualTo("CRT");
        
        verify(pedidoRepository).save(any(Pedido.class));
        verify(logService).registrarLog(eq(1), contains("Pedido creado"));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-02: Crear pedido con administrador no encontrado")
    void testCrearPedido_AdminNoEncontrado() {
        // Arrange
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.crearPedido(pedidoDTO, 999))
                .isInstanceOf(PedidoBusinessException.class)
                .hasMessage(PedidoConstants.ERROR_ADMIN_NO_ENCONTRADO);

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-03: Crear pedido con producto no encontrado")
    void testCrearPedido_ProductoNoEncontrado() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(admin));
        when(productoRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.crearPedido(pedidoDTO, 1))
                .isInstanceOf(PedidoBusinessException.class)
                .hasMessage(PedidoConstants.ERROR_PRODUCTO_NO_ENCONTRADO);

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-04: Crear pedido con estado ACTIVO notifica WhatsApp")
    void testCrearPedido_EstadoActivo_NotificaWhatsApp() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(admin));
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(helper.crearProductoPedido(any(Pedido.class), eq(producto), eq(10), eq(50)))
                .thenReturn(productoPedido);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setIdPedido(1);
            p.setEstadoPedido(EstadoPedidoEnum.ACT);
            return p;
        });

        // Act
        pedidoService.crearPedido(pedidoDTO, 1);

        // Assert
        verify(whatsAppService).notificarNuevoPedido(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-05: Actualizar estado de pedido exitosamente")
    void testActualizarEstado_Exitoso() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.CRT);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(validator).validarTransicionEstado(
                eq(EstadoPedidoEnum.CRT), eq(EstadoPedidoEnum.ACT));

        // Act
        PedidoDTO resultado = pedidoService.actualizarEstado(1, "ACT");

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstadoPedido()).isEqualTo("ACT");
        
        verify(validator).validarTransicionEstado(EstadoPedidoEnum.CRT, EstadoPedidoEnum.ACT);
        verify(logService).registrarCambioEstadoPedido(eq(1), eq(1), eq("CRT"), eq("ACT"));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-06: Actualizar estado a ACTIVO genera hash y notifica")
    void testActualizarEstado_CambioAActivo_GeneraHashYNotifica() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.CRT);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(hashids.encode(1L)).thenReturn("abc123");
        doNothing().when(validator).validarTransicionEstado(
                eq(EstadoPedidoEnum.CRT), eq(EstadoPedidoEnum.ACT));

        // Act
        pedidoService.actualizarEstado(1, "ACT");

        // Assert
        verify(hashids).encode(1L);
        verify(whatsAppService).notificarNuevoPedido(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-07: Actualizar estado con pedido no encontrado")
    void testActualizarEstado_PedidoNoEncontrado() {
        // Arrange
        when(pedidoRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.actualizarEstado(999, "ACT"))
                .isInstanceOf(PedidoBusinessException.class)
                .hasMessage(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO);

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-08: Actualizar pedido en estado CREADO")
    void testActualizarPedido_EstadoCreado() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.CRT);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoDTO actualizacion = new PedidoDTO();
        actualizacion.setFechaCierre(LocalDate.now().plusDays(14));
        actualizacion.setEstadoPedido("ACT");

        // Act
        PedidoDTO resultado = pedidoService.actualizarPedido(1, actualizacion);

        // Assert
        assertThat(resultado).isNotNull();
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-09: Actualizar pedido en estado ACTIVO solo permite cambiar fecha")
    void testActualizarPedido_EstadoActivo_SoloFecha() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.ACT);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoDTO actualizacion = new PedidoDTO();
        actualizacion.setFechaCierre(LocalDate.now().plusDays(10));

        // Act
        PedidoDTO resultado = pedidoService.actualizarPedido(1, actualizacion);

        // Assert
        assertThat(resultado).isNotNull();
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-10: Actualizar pedido ACTIVO sin fecha lanza excepción")
    void testActualizarPedido_EstadoActivo_SinFecha() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.ACT);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        PedidoDTO actualizacion = new PedidoDTO();
        // No se establece fecha

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.actualizarPedido(1, actualizacion))
                .isInstanceOf(PedidoBusinessException.class)
                .hasMessage(PedidoConstants.ERROR_PEDIDO_ACTIVO_SOLO_FECHA);
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-11: Actualizar pedido en estado no modificable lanza excepción")
    void testActualizarPedido_EstadoNoModificable() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.ENT); // Entregado
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        PedidoDTO actualizacion = new PedidoDTO();
        actualizacion.setFechaCierre(LocalDate.now().plusDays(10));

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.actualizarPedido(1, actualizacion))
                .isInstanceOf(PedidoBusinessException.class)
                .hasMessageContaining("No se puede modificar el pedido en estado");
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-12: Agregar producto exitosamente")
    void testAgregarProducto_Exitoso() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.CRT);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(2)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(validator).validarPedidoEstadoCreado(any(Pedido.class), eq("agregar"));
        doNothing().when(helper).validarProductoNoExiste(any(Pedido.class), eq(2));
        when(helper.crearProductoPedido(any(Pedido.class), eq(producto), eq(5), eq(20)))
                .thenReturn(productoPedido);

        ProductoPedidoDTO nuevoProducto = new ProductoPedidoDTO(2, 5, 20);

        // Act
        PedidoDTO resultado = pedidoService.agregarProducto(1, nuevoProducto);

        // Assert
        assertThat(resultado).isNotNull();
        verify(validator).validarPedidoEstadoCreado(any(Pedido.class), eq("agregar"));
        verify(helper).validarProductoNoExiste(any(Pedido.class), eq(2));
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-13: Eliminar producto exitosamente")
    void testEliminarProducto_Exitoso() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.CRT);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(helper.buscarProductoEnPedido(any(Pedido.class), eq(1)))
                .thenReturn(productoPedido);
        doNothing().when(validator).validarPedidoEstadoCreado(any(Pedido.class), eq("eliminar"));

        // Act
        PedidoDTO resultado = pedidoService.eliminarProducto(1, 1);

        // Assert
        assertThat(resultado).isNotNull();
        verify(validator).validarPedidoEstadoCreado(any(Pedido.class), eq("eliminar"));
        verify(helper).buscarProductoEnPedido(any(Pedido.class), eq(1));
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-14: Modificar producto exitosamente")
    void testModificarProducto_Exitoso() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.CRT);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(helper.buscarProductoEnPedido(any(Pedido.class), eq(1)))
                .thenReturn(productoPedido);
        doNothing().when(validator).validarPedidoEstadoCreado(any(Pedido.class), eq("modificar"));
        doNothing().when(helper).actualizarCantidades(eq(productoPedido), any(ProductoPedidoDTO.class));

        ProductoPedidoDTO modificacion = new ProductoPedidoDTO(1, 15, 60);

        // Act
        PedidoDTO resultado = pedidoService.modificarProducto(1, 1, modificacion);

        // Assert
        assertThat(resultado).isNotNull();
        verify(validator).validarPedidoEstadoCreado(any(Pedido.class), eq("modificar"));
        verify(helper).actualizarCantidades(eq(productoPedido), any(ProductoPedidoDTO.class));
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-15: Cancelar pedido y solicitudes asociadas")
    void testCancelarPedido_Exitoso() {
        // Arrange
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud solicitud1 = new Solicitud();
        solicitud1.setEstadoSolicitud(EstadoSolicitudEnum.PDP);
        Solicitud solicitud2 = new Solicitud();
        solicitud2.setEstadoSolicitud(EstadoSolicitudEnum.PGD);

        when(solicitudRepository.findByPedido_IdPedido(1))
                .thenReturn(Arrays.asList(solicitud1, solicitud2));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        pedidoService.cancelarPedido(1);

        // Assert
        verify(pedidoRepository).save(argThat(p -> p.getEstadoPedido() == EstadoPedidoEnum.CRM));
        verify(solicitudRepository, times(2)).save(any(Solicitud.class));
        verify(whatsAppService).notificarCancelacionPedido(any(Pedido.class), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-16: Listar pedidos exitosamente")
    void testListarPedidos_Exitoso() {
        // Arrange
        Pedido pedido2 = new Pedido();
        pedido2.setIdPedido(2);
        pedido2.setAdmin(admin);
        pedido2.setEstadoPedido(EstadoPedidoEnum.ACT);
        pedido2.setFechaCreado(LocalDate.now());
        pedido2.setProductos(new HashSet<>());

        when(pedidoRepository.findAll()).thenReturn(Arrays.asList(pedido, pedido2));

        // Act
        List<PedidoDTO> resultado = pedidoService.listarPedidos();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getIdPedido()).isEqualTo(1);
        assertThat(resultado.get(1).getIdPedido()).isEqualTo(2);
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-17: Obtener pedido por ID exitosamente")
    void testObtenerPedidoPorId_Exitoso() {
        // Arrange
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        // Act
        PedidoDTO resultado = pedidoService.obtenerPedidoPorId(1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPedido()).isEqualTo(1);
        assertThat(resultado.getIdAdmin()).isEqualTo(1);
        assertThat(resultado.getEstadoPedido()).isEqualTo("CRT");
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-18: Obtener pedido por ID no encontrado")
    void testObtenerPedidoPorId_NoEncontrado() {
        // Arrange
        when(pedidoRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.obtenerPedidoPorId(999))
                .isInstanceOf(PedidoBusinessException.class)
                .hasMessage(PedidoConstants.ERROR_PEDIDO_NO_ENCONTRADO);
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-19: Generar URL hash para pedido activo")
    void testGenerarUrlHash_Exitoso() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.ACT);
        pedido.setUrlHash(null);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(hashids.encode(1L)).thenReturn("abc123xyz");
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(validator).validarPedidoEstadoActivo(any(Pedido.class));

        // Act
        String hash = pedidoService.generarUrlHash(1);

        // Assert
        assertThat(hash).isEqualTo("abc123xyz");
        verify(hashids).encode(1L);
        verify(pedidoRepository).save(argThat(p -> "abc123xyz".equals(p.getUrlHash())));
        verify(logService).registrarLog(eq(1), contains("URL hash generada"));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-20: Generar URL hash devuelve existente si ya tiene")
    void testGenerarUrlHash_YaExiste_DevuelveExistente() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.ACT);
        pedido.setUrlHash("existente123");
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        doNothing().when(validator).validarPedidoEstadoActivo(any(Pedido.class));

        // Act
        String hash = pedidoService.generarUrlHash(1);

        // Assert
        assertThat(hash).isEqualTo("existente123");
        verify(hashids, never()).encode(anyLong());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-21: Obtener pedido por hash exitosamente")
    void testObtenerPedidoPorHash_Exitoso() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.ACT);
        pedido.setUrlHash("abc123xyz");
        when(hashids.decode("abc123xyz")).thenReturn(new long[]{1L});
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        doNothing().when(validator).validarPedidoEstadoActivo(any(Pedido.class));

        // Act
        PedidoDTO resultado = pedidoService.obtenerPedidoPorHash("abc123xyz");

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPedido()).isEqualTo(1);
        assertThat(resultado.getEstadoPedido()).isEqualTo("ACT");
        verify(hashids).decode("abc123xyz");
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-22: Obtener pedido por hash vacío lanza excepción")
    void testObtenerPedidoPorHash_HashVacio() {
        // Act & Assert
        assertThatThrownBy(() -> pedidoService.obtenerPedidoPorHash(""))
                .isInstanceOf(PedidoBusinessException.class)
                .hasMessage(PedidoConstants.ERROR_HASH_VACIO);

        verify(hashids, never()).decode(anyString());
    }

    @Test
    @DisplayName("TC-UNIT-PEDIDO-23: Obtener pedido por hash inválido lanza excepción")
    void testObtenerPedidoPorHash_HashInvalido() {
        // Arrange
        when(hashids.decode("invalido")).thenReturn(new long[]{});

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.obtenerPedidoPorHash("invalido"))
                .isInstanceOf(PedidoBusinessException.class)
                .hasMessage(PedidoConstants.ERROR_HASH_INVALIDO);

        verify(hashids).decode("invalido");
    }
}
