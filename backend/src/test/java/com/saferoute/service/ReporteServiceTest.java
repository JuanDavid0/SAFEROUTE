package com.saferoute.service;

import com.saferoute.dto.reporte.*;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.helper.AgrupamientoReporteHelper;
import com.saferoute.service.helper.CalculosFinancierosHelper;
import com.saferoute.service.impl.ReporteServiceImpl;
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
 * Tests de Reporte Service
 * 
 * Cobertura:
 * - Generar reporte de ingresos (con y sin datos)
 * - Obtener productos más vendidos
 * - Obtener productos con mayor ganancia
 * - Obtener clientes frecuentes
 * - Obtener pedidos en curso
 * - Obtener resumen de estadísticas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Reporte Service")
class ReporteServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CalculosFinancierosHelper calculosHelper;

    @Mock
    private AgrupamientoReporteHelper agrupamientoHelper;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    private Usuario cliente;
    private Pedido pedido;
    private Producto producto1;
    private Producto producto2;
    private Solicitud solicitud;
    private SolicitudProducto solicitudProducto;

    @BeforeEach
    void setUp() {
        // Cliente
        cliente = new Usuario();
        cliente.setIdUsuario(1);
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setCedula("1234567890");
        cliente.setTelefono("3001234567");

        // Pedido
        pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setEstadoPedido(EstadoPedidoEnum.ACT);
        pedido.setFechaCreado(LocalDate.now().minusDays(10));
        pedido.setFechaCierre(LocalDate.now().plusDays(5));

        // Productos
        producto1 = new Producto();
        producto1.setIdProducto(1);
        producto1.setNombreProducto("Producto 1");
        producto1.setTipoProducto("Categoria A");
        producto1.setDescripcionProducto("Descripción 1");
        producto1.setPrecioUnitario(new BigDecimal("100.00"));
        producto1.setCostoUnitario(new BigDecimal("60.00"));

        producto2 = new Producto();
        producto2.setIdProducto(2);
        producto2.setNombreProducto("Producto 2");
        producto2.setTipoProducto("Categoria B");
        producto2.setDescripcionProducto("Descripción 2");
        producto2.setPrecioUnitario(new BigDecimal("200.00"));
        producto2.setCostoUnitario(new BigDecimal("120.00"));

        // SolicitudProducto
        solicitudProducto = new SolicitudProducto();
        solicitudProducto.setProducto(producto1);
        solicitudProducto.setCantidadSolicitada(10);
        solicitudProducto.setPrecio(new BigDecimal("100.00"));

        // Solicitud
        solicitud = new Solicitud();
        solicitud.setIdSolicitud(1);
        solicitud.setCliente(cliente);
        solicitud.setPedido(pedido);
        solicitud.setEstadoSolicitud(EstadoSolicitudEnum.PGD);
        solicitud.setFechaSolicitud(LocalDate.now().minusDays(5));
        solicitud.setProductos(new HashSet<>(Collections.singletonList(solicitudProducto)));
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-01: Generar reporte de ingresos con datos")
    void testGenerarReporteIngresos_ConDatos() {
        // Arrange
        LocalDate fechaInicio = LocalDate.now().minusDays(30);
        LocalDate fechaFin = LocalDate.now();
        String agrupacion = "mensual";

        List<Solicitud> solicitudes = Collections.singletonList(solicitud);
        List<ReporteIngresosDTO.DatosPeriodo> datosPeriodo = new ArrayList<>();

        when(solicitudRepository.findSolicitudesPagadasEnRango(fechaInicio, fechaFin))
                .thenReturn(solicitudes);
        when(calculosHelper.calcularIngresosSolicitud(solicitud))
                .thenReturn(new BigDecimal("1000.00"));
        when(calculosHelper.calcularCostosSolicitud(solicitud))
                .thenReturn(new BigDecimal("600.00"));
        when(agrupamientoHelper.agruparPorPeriodo(solicitudes, agrupacion))
                .thenReturn(datosPeriodo);

        // Act
        ReporteIngresosDTO resultado = reporteService.generarReporteIngresos(fechaInicio, fechaFin, agrupacion);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getFechaInicio()).isEqualTo(fechaInicio);
        assertThat(resultado.getFechaFin()).isEqualTo(fechaFin);
        assertThat(resultado.getAgrupacion()).isEqualTo(agrupacion);
        assertThat(resultado.getTotalIngresos()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(resultado.getTotalCostos()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(resultado.getGananciaNeta()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(resultado.getTotalSolicitudesPagadas()).isEqualTo(1);
        
        verify(solicitudRepository).findSolicitudesPagadasEnRango(fechaInicio, fechaFin);
        verify(calculosHelper).calcularIngresosSolicitud(solicitud);
        verify(calculosHelper).calcularCostosSolicitud(solicitud);
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-02: Generar reporte de ingresos sin datos")
    void testGenerarReporteIngresos_SinDatos() {
        // Arrange
        LocalDate fechaInicio = LocalDate.now().minusDays(30);
        LocalDate fechaFin = LocalDate.now();
        String agrupacion = "diario";

        when(solicitudRepository.findSolicitudesPagadasEnRango(fechaInicio, fechaFin))
                .thenReturn(Collections.emptyList());

        // Act
        ReporteIngresosDTO resultado = reporteService.generarReporteIngresos(fechaInicio, fechaFin, agrupacion);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalIngresos()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getTotalCostos()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getGananciaNeta()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getTotalSolicitudesPagadas()).isEqualTo(0);
        assertThat(resultado.getDatosPorPeriodo()).isEmpty();
        
        verify(solicitudRepository).findSolicitudesPagadasEnRango(fechaInicio, fechaFin);
        verify(calculosHelper, never()).calcularIngresosSolicitud(any());
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-03: Obtener productos más vendidos")
    void testObtenerProductosMasVendidos_Exitoso() {
        // Arrange
        List<Solicitud> solicitudes = Collections.singletonList(solicitud);
        
        when(solicitudRepository.findByEstadoSolicitud(EstadoSolicitudEnum.PGD))
                .thenReturn(solicitudes);
        when(calculosHelper.calcularMontoProducto(solicitudProducto))
                .thenReturn(new BigDecimal("1000.00"));
        when(calculosHelper.calcularCostoProducto(solicitudProducto))
                .thenReturn(new BigDecimal("600.00"));

        // Act
        List<ProductoVendidoDTO> resultado = reporteService.obtenerProductosMasVendidos(5);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        
        ProductoVendidoDTO dto = resultado.get(0);
        assertThat(dto.getIdProducto()).isEqualTo(1);
        assertThat(dto.getNombreProducto()).isEqualTo("Producto 1");
        assertThat(dto.getCantidadTotalVendida()).isEqualTo(10);
        assertThat(dto.getIngresosGenerados()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(dto.getGananciaNeta()).isEqualByComparingTo(new BigDecimal("400.00"));
        
        verify(solicitudRepository).findByEstadoSolicitud(EstadoSolicitudEnum.PGD);
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-04: Obtener productos más vendidos con límite por defecto")
    void testObtenerProductosMasVendidos_LimitePorDefecto() {
        // Arrange
        when(solicitudRepository.findByEstadoSolicitud(EstadoSolicitudEnum.PGD))
                .thenReturn(Collections.emptyList());

        // Act
        List<ProductoVendidoDTO> resultado = reporteService.obtenerProductosMasVendidos(null);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        
        verify(solicitudRepository).findByEstadoSolicitud(EstadoSolicitudEnum.PGD);
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-05: Obtener productos con mayor ganancia")
    void testObtenerProductosMayorGanancia_Exitoso() {
        // Arrange
        when(solicitudRepository.findByEstadoSolicitud(EstadoSolicitudEnum.PGD))
                .thenReturn(Collections.singletonList(solicitud));
        when(calculosHelper.calcularMontoProducto(solicitudProducto))
                .thenReturn(new BigDecimal("1000.00"));
        when(calculosHelper.calcularCostoProducto(solicitudProducto))
                .thenReturn(new BigDecimal("600.00"));

        // Act
        List<ProductoVendidoDTO> resultado = reporteService.obtenerProductosMayorGanancia(3);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getGananciaNeta()).isEqualByComparingTo(new BigDecimal("400.00"));
        
        verify(solicitudRepository).findByEstadoSolicitud(EstadoSolicitudEnum.PGD);
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-06: Obtener clientes frecuentes")
    void testObtenerClientesFrecuentes_Exitoso() {
        // Arrange
        Solicitud solicitud2 = new Solicitud();
        solicitud2.setIdSolicitud(2);
        solicitud2.setCliente(cliente);
        solicitud2.setPedido(pedido);
        solicitud2.setEstadoSolicitud(EstadoSolicitudEnum.PDP);
        solicitud2.setFechaSolicitud(LocalDate.now().minusDays(3));
        solicitud2.setProductos(new HashSet<>());

        List<Solicitud> solicitudes = Arrays.asList(solicitud, solicitud2);

        when(solicitudRepository.findAll()).thenReturn(solicitudes);
        when(calculosHelper.calcularMontoSolicitud(solicitud))
                .thenReturn(new BigDecimal("1000.00"));

        // Act
        List<ClienteFrecuenteDTO> resultado = reporteService.obtenerClientesFrecuentes(5);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        
        ClienteFrecuenteDTO dto = resultado.get(0);
        assertThat(dto.getCedula()).isEqualTo("1234567890");
        assertThat(dto.getNombreCompleto()).contains("Juan").contains("Pérez");
        assertThat(dto.getTotalSolicitudes()).isEqualTo(2);
        assertThat(dto.getSolicitudesPagadas()).isEqualTo(1);
        assertThat(dto.getMontoTotalGastado()).isEqualByComparingTo(new BigDecimal("1000.00"));
        
        verify(solicitudRepository).findAll();
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-07: Obtener pedidos en curso")
    void testObtenerPedidosEnCurso_Exitoso() {
        // Arrange
        ProductoPedido productoPedido = new ProductoPedido();
        productoPedido.setProducto(producto1);
        pedido.setProductos(new HashSet<>(Collections.singletonList(productoPedido)));

        when(pedidoRepository.findPedidosEnCurso())
                .thenReturn(Collections.singletonList(pedido));
        when(solicitudRepository.findByPedido_IdPedido(1))
                .thenReturn(Collections.singletonList(solicitud));
        when(calculosHelper.calcularMontoSolicitud(solicitud))
                .thenReturn(new BigDecimal("1000.00"));

        // Act
        List<PedidoEnCursoDTO> resultado = reporteService.obtenerPedidosEnCurso();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        
        PedidoEnCursoDTO dto = resultado.get(0);
        assertThat(dto.getIdPedido()).isEqualTo(1);
        assertThat(dto.getEstadoPedido()).isEqualTo("ACT");
        assertThat(dto.getTotalSolicitudes()).isEqualTo(1);
        assertThat(dto.getSolicitudesPagadas()).isEqualTo(1);
        assertThat(dto.getMontoTotalEstimado()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(dto.getMontoPagado()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(dto.getProgresoPercent()).isEqualTo(100);
        
        verify(pedidoRepository).findPedidosEnCurso();
        verify(solicitudRepository).findByPedido_IdPedido(1);
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-08: Obtener pedidos en curso sin solicitudes")
    void testObtenerPedidosEnCurso_SinSolicitudes() {
        // Arrange
        when(pedidoRepository.findPedidosEnCurso())
                .thenReturn(Collections.singletonList(pedido));
        when(solicitudRepository.findByPedido_IdPedido(1))
                .thenReturn(Collections.emptyList());

        // Act
        List<PedidoEnCursoDTO> resultado = reporteService.obtenerPedidosEnCurso();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        
        PedidoEnCursoDTO dto = resultado.get(0);
        assertThat(dto.getTotalSolicitudes()).isEqualTo(0);
        assertThat(dto.getProgresoPercent()).isEqualTo(0);
        assertThat(dto.getMontoTotalEstimado()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-09: Obtener resumen de estadísticas completo")
    void testObtenerResumenEstadisticas_Exitoso() {
        // Arrange
        ProductoPedido productoPedido = new ProductoPedido();
        productoPedido.setProducto(producto1);
        pedido.setProductos(new HashSet<>(Collections.singletonList(productoPedido)));

        // Mocks para pedidos
        when(pedidoRepository.count()).thenReturn(10L);
        when(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.ACT)).thenReturn(2L);
        when(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.RTA)).thenReturn(1L);
        when(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.ADU)).thenReturn(1L);
        when(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.ENT)).thenReturn(5L);
        when(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.CRM)).thenReturn(0L);
        when(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.CRA)).thenReturn(1L);

        // Mocks para solicitudes
        when(solicitudRepository.count()).thenReturn(50L);
        when(solicitudRepository.countByEstadoSolicitud(EstadoSolicitudEnum.PDP)).thenReturn(10L);
        when(solicitudRepository.countByEstadoSolicitud(EstadoSolicitudEnum.PGD)).thenReturn(35L);
        when(solicitudRepository.countByEstadoSolicitud(EstadoSolicitudEnum.CAN)).thenReturn(5L);
        
        when(solicitudRepository.findByEstadoSolicitud(EstadoSolicitudEnum.PGD))
                .thenReturn(Collections.singletonList(solicitud));
        when(calculosHelper.calcularIngresosSolicitud(solicitud))
                .thenReturn(new BigDecimal("1000.00"));
        when(calculosHelper.calcularCostosSolicitud(solicitud))
                .thenReturn(new BigDecimal("600.00"));

        // Mocks para clientes y productos
        when(usuarioRepository.count()).thenReturn(25L);
        when(solicitudRepository.countClientesConSolicitudes()).thenReturn(20L);
        when(productoRepository.count()).thenReturn(15L);
        when(pedidoRepository.findByEstadoPedido(EstadoPedidoEnum.ACT))
                .thenReturn(Collections.singletonList(pedido));

        // Act
        ResumenEstadisticasDTO resultado = reporteService.obtenerResumenEstadisticas();

        // Assert
        assertThat(resultado).isNotNull();
        
        // Estadísticas de pedidos
        assertThat(resultado.getTotalPedidos()).isEqualTo(10);
        assertThat(resultado.getPedidosActivos()).isEqualTo(2);
        assertThat(resultado.getPedidosEnCurso()).isEqualTo(2);
        assertThat(resultado.getPedidosEntregados()).isEqualTo(5);
        assertThat(resultado.getPedidosCancelados()).isEqualTo(1);
        
        // Estadísticas de solicitudes
        assertThat(resultado.getTotalSolicitudes()).isEqualTo(50);
        assertThat(resultado.getSolicitudesPendientes()).isEqualTo(10);
        assertThat(resultado.getSolicitudesPagadas()).isEqualTo(35);
        assertThat(resultado.getSolicitudesRechazadas()).isEqualTo(5);
        
        // Estadísticas financieras
        assertThat(resultado.getIngresosTotales()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(resultado.getCostosTotales()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(resultado.getGananciaNeta()).isEqualByComparingTo(new BigDecimal("400.00"));
        
        // Estadísticas de clientes y productos
        assertThat(resultado.getTotalClientes()).isEqualTo(25);
        assertThat(resultado.getClientesActivos()).isEqualTo(20);
        assertThat(resultado.getTotalProductos()).isEqualTo(15);
        assertThat(resultado.getProductosActivos()).isEqualTo(1);
        
        verify(pedidoRepository).count();
        verify(solicitudRepository).count();
        verify(usuarioRepository).count();
        verify(productoRepository).count();
    }

    @Test
    @DisplayName("TC-UNIT-REPORTE-10: Obtener resumen de estadísticas sin datos")
    void testObtenerResumenEstadisticas_SinDatos() {
        // Arrange
        when(pedidoRepository.count()).thenReturn(0L);
        when(pedidoRepository.countByEstadoPedido(any())).thenReturn(0L);
        when(solicitudRepository.count()).thenReturn(0L);
        when(solicitudRepository.countByEstadoSolicitud(any())).thenReturn(0L);
        when(solicitudRepository.findByEstadoSolicitud(EstadoSolicitudEnum.PGD))
                .thenReturn(Collections.emptyList());
        when(usuarioRepository.count()).thenReturn(0L);
        when(solicitudRepository.countClientesConSolicitudes()).thenReturn(0L);
        when(productoRepository.count()).thenReturn(0L);
        when(pedidoRepository.findByEstadoPedido(EstadoPedidoEnum.ACT))
                .thenReturn(Collections.emptyList());

        // Act
        ResumenEstadisticasDTO resultado = reporteService.obtenerResumenEstadisticas();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalPedidos()).isEqualTo(0);
        assertThat(resultado.getTotalSolicitudes()).isEqualTo(0);
        assertThat(resultado.getIngresosTotales()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getGananciaNeta()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getTotalClientes()).isEqualTo(0);
        assertThat(resultado.getTotalProductos()).isEqualTo(0);
    }
}
