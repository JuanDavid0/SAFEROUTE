package com.saferoute.service;

import com.saferoute.dto.ConsolidacionDTO;
import com.saferoute.dto.ConsolidacionProductoDTO;
import com.saferoute.exception.ConsolidacionBusinessException;
import com.saferoute.helper.ConsolidacionCalculosHelper;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.PedidoRepository;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.service.impl.ConsolidacionServiceImpl;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.validator.ConsolidacionValidator;
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
 * Tests de Consolidacion Service
 * 
 * Cobertura:
 * - Consolidar pedido exitoso
 * - Pedido no encontrado
 * - Pedido sin solicitudes pagadas
 * - Validación de estado del pedido
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Consolidacion Service")
class ConsolidacionServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private ILogService logService;

    @Mock
    private ConsolidacionValidator consolidacionValidator;

    @Mock
    private ConsolidacionCalculosHelper calculosHelper;

    @InjectMocks
    private ConsolidacionServiceImpl consolidacionService;

    private Usuario admin;
    private Pedido pedido;
    private Producto producto1;
    private Producto producto2;
    private Solicitud solicitud1;
    private Solicitud solicitud2;
    private SolicitudProducto solicitudProducto1;
    private SolicitudProducto solicitudProducto2;
    private Map<String, Object> opciones;

    @BeforeEach
    void setUp() {
        // Admin
        admin = new Usuario();
        admin.setIdUsuario(1);
        admin.setNombres("Admin");
        admin.setApellidos("User");

        // Pedido ACT
        pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setEstadoPedido(EstadoPedidoEnum.ACT);
        pedido.setFechaCreado(LocalDate.now().minusDays(5));
        pedido.setFechaCierre(LocalDate.now().plusDays(5));
        pedido.setAdmin(admin);

        // Productos
        producto1 = new Producto();
        producto1.setIdProducto(1);
        producto1.setNombreProducto("Producto 1");
        producto1.setPrecioUnitario(new BigDecimal("100.00"));
        producto1.setCostoUnitario(new BigDecimal("60.00"));

        producto2 = new Producto();
        producto2.setIdProducto(2);
        producto2.setNombreProducto("Producto 2");
        producto2.setPrecioUnitario(new BigDecimal("200.00"));
        producto2.setCostoUnitario(new BigDecimal("120.00"));

        // SolicitudProducto
        solicitudProducto1 = new SolicitudProducto();
        solicitudProducto1.setProducto(producto1);
        solicitudProducto1.setCantidadSolicitada(10);
        solicitudProducto1.setPrecio(new BigDecimal("100.00"));

        solicitudProducto2 = new SolicitudProducto();
        solicitudProducto2.setProducto(producto2);
        solicitudProducto2.setCantidadSolicitada(5);
        solicitudProducto2.setPrecio(new BigDecimal("200.00"));

        // Solicitudes PGD
        solicitud1 = new Solicitud();
        solicitud1.setIdSolicitud(1);
        solicitud1.setPedido(pedido);
        solicitud1.setEstadoSolicitud(EstadoSolicitudEnum.PGD);
        solicitud1.setFechaSolicitud(LocalDate.now().minusDays(3));
        solicitud1.setProductos(new HashSet<>(Collections.singletonList(solicitudProducto1)));

        solicitud2 = new Solicitud();
        solicitud2.setIdSolicitud(2);
        solicitud2.setPedido(pedido);
        solicitud2.setEstadoSolicitud(EstadoSolicitudEnum.PGD);
        solicitud2.setFechaSolicitud(LocalDate.now().minusDays(2));
        solicitud2.setProductos(new HashSet<>(Collections.singletonList(solicitudProducto2)));

        // Opciones (puede estar vacío o con parámetros adicionales)
        opciones = new HashMap<>();
    }

    @Test
    @DisplayName("TC-UNIT-CONSOLIDACION-01: Consolidar pedido exitosamente")
    void testConsolidarPedido_Exitoso() {
        // Arrange
        List<Solicitud> solicitudesPagadas = Arrays.asList(solicitud1, solicitud2);
        BigDecimal montoTotal = new BigDecimal("2000.00");
        
        ConsolidacionProductoDTO productoDTO1 = new ConsolidacionProductoDTO();
        productoDTO1.setIdProducto(1);
        productoDTO1.setNombreProducto("Producto 1");
        productoDTO1.setCantidadTotal(10);
        
        ConsolidacionProductoDTO productoDTO2 = new ConsolidacionProductoDTO();
        productoDTO2.setIdProducto(2);
        productoDTO2.setNombreProducto("Producto 2");
        productoDTO2.setCantidadTotal(5);
        
        List<ConsolidacionProductoDTO> productosConsolidados = Arrays.asList(productoDTO1, productoDTO2);

        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        doNothing().when(consolidacionValidator).validarPedidoActivo(pedido);
        when(solicitudRepository.findByPedido_IdPedidoAndEstadoSolicitud(1, EstadoSolicitudEnum.PGD))
                .thenReturn(solicitudesPagadas);
        doNothing().when(consolidacionValidator).validarSolicitudesPagadas(solicitudesPagadas, 1);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(calculosHelper.calcularMontoTotal(solicitudesPagadas)).thenReturn(montoTotal);
        when(calculosHelper.consolidarProductos(solicitudesPagadas)).thenReturn(productosConsolidados);
        doNothing().when(logService).registrarCambioEstadoPedido(anyInt(), anyInt(), anyString(), anyString());

        // Act
        ConsolidacionDTO resultado = consolidacionService.consolidarPedido(1, opciones);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPedido()).isEqualTo(1);
        assertThat(resultado.getNuevoEstado()).isEqualTo("RTA");
        assertThat(resultado.getTotalSolicitudes()).isEqualTo(2);
        assertThat(resultado.getMontoTotal()).isEqualByComparingTo(montoTotal);
        assertThat(resultado.getProductos()).hasSize(2);
        assertThat(resultado.getMensaje()).isNotNull();

        // Verify estado cambió a RTA
        verify(pedidoRepository).save(argThat(p -> 
            p.getEstadoPedido() == EstadoPedidoEnum.RTA
        ));
        verify(logService).registrarCambioEstadoPedido(1, 1, "ACT", "RTA");
        verify(consolidacionValidator).validarPedidoActivo(pedido);
        verify(consolidacionValidator).validarSolicitudesPagadas(solicitudesPagadas, 1);
    }

    @Test
    @DisplayName("TC-UNIT-CONSOLIDACION-02: Error al consolidar pedido inexistente")
    void testConsolidarPedido_PedidoNoEncontrado() {
        // Arrange
        when(pedidoRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> consolidacionService.consolidarPedido(999, opciones))
                .isInstanceOf(ConsolidacionBusinessException.class)
                .hasMessageContaining("999");

        verify(pedidoRepository).findById(999);
        verify(consolidacionValidator, never()).validarPedidoActivo(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-UNIT-CONSOLIDACION-03: Error al consolidar pedido sin solicitudes pagadas")
    void testConsolidarPedido_SinSolicitudesPagadas() {
        // Arrange
        List<Solicitud> solicitudesVacias = Collections.emptyList();

        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        doNothing().when(consolidacionValidator).validarPedidoActivo(pedido);
        when(solicitudRepository.findByPedido_IdPedidoAndEstadoSolicitud(1, EstadoSolicitudEnum.PGD))
                .thenReturn(solicitudesVacias);
        doThrow(new ConsolidacionBusinessException("No hay solicitudes pagadas para consolidar"))
                .when(consolidacionValidator).validarSolicitudesPagadas(solicitudesVacias, 1);

        // Act & Assert
        assertThatThrownBy(() -> consolidacionService.consolidarPedido(1, opciones))
                .isInstanceOf(ConsolidacionBusinessException.class)
                .hasMessageContaining("solicitudes pagadas");

        verify(pedidoRepository).findById(1);
        verify(consolidacionValidator).validarPedidoActivo(pedido);
        verify(consolidacionValidator).validarSolicitudesPagadas(solicitudesVacias, 1);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-UNIT-CONSOLIDACION-04: Error al consolidar pedido no activo")
    void testConsolidarPedido_PedidoNoActivo() {
        // Arrange
        pedido.setEstadoPedido(EstadoPedidoEnum.RTA); // Ya está en ruta

        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        doThrow(new ConsolidacionBusinessException("El pedido debe estar en estado ACT para consolidar"))
                .when(consolidacionValidator).validarPedidoActivo(pedido);

        // Act & Assert
        assertThatThrownBy(() -> consolidacionService.consolidarPedido(1, opciones))
                .isInstanceOf(ConsolidacionBusinessException.class)
                .hasMessageContaining("ACT");

        verify(pedidoRepository).findById(1);
        verify(consolidacionValidator).validarPedidoActivo(pedido);
        verify(solicitudRepository, never()).findByPedido_IdPedidoAndEstadoSolicitud(anyInt(), any());
        verify(pedidoRepository, never()).save(any());
    }
}
