package com.saferoute.service;

import com.saferoute.dto.ProductoDTO;
import com.saferoute.exception.ProductoBusinessException;
import com.saferoute.helper.ProductoMapper;
import com.saferoute.helper.UsuarioAutenticadoHelper;
import com.saferoute.model.Producto;
import com.saferoute.repository.ProductoRepository;
import com.saferoute.service.impl.ProductoServiceImpl;
import com.saferoute.service.interfaces.ILogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductoServiceImpl
 * 
 * Pruebas de gestión de productos (CRUD completo)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Producto Service")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ILogService logService;

    @Mock
    private ProductoMapper productoMapper;

    @Mock
    private UsuarioAutenticadoHelper usuarioHelper;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto;
    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        // Configurar producto de prueba
        producto = new Producto();
        producto.setIdProducto(1);
        producto.setNombreProducto("Caja de Cartón Grande");
        producto.setTipoProducto("EMPAQUE");
        producto.setDescripcionProducto("Caja resistente para envíos pesados");
        producto.setPrecioUnitario(new BigDecimal("5000.00"));
        producto.setCostoUnitario(new BigDecimal("3000.00"));
        producto.setUrlImagen("https://example.com/caja.jpg");
        producto.setEstadoProducto("ACTIVO");

        // Configurar DTO de prueba
        productoDTO = new ProductoDTO();
        productoDTO.setIdProducto(1);
        productoDTO.setNombreProducto("Caja de Cartón Grande");
        productoDTO.setTipoProducto("EMPAQUE");
        productoDTO.setDescripcionProducto("Caja resistente para envíos pesados");
        productoDTO.setPrecioUnitario(new BigDecimal("5000.00"));
        productoDTO.setCostoUnitario(new BigDecimal("3000.00"));
        productoDTO.setUrlImagen("https://example.com/caja.jpg");
    }

    // ==================== CREAR PRODUCTO ====================

    @Test
    @DisplayName("TC-UNIT-PROD-001: Crear producto exitosamente")
    void testCrearProducto_Success() {
        // Arrange
        when(productoMapper.toEntity(productoDTO)).thenReturn(producto);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(productoMapper.toDTO(producto)).thenReturn(productoDTO);
        when(usuarioHelper.obtenerUsuarioAutenticadoId()).thenReturn(100);
        doNothing().when(logService).registrarLog(anyInt(), anyString());

        // Act
        ProductoDTO result = productoService.crearProducto(productoDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIdProducto()).isEqualTo(1);
        assertThat(result.getNombreProducto()).isEqualTo("Caja de Cartón Grande");
        assertThat(result.getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("5000.00"));

        verify(productoMapper).toEntity(productoDTO);
        verify(productoRepository).save(any(Producto.class));
        verify(productoMapper).toDTO(producto);
        verify(usuarioHelper).obtenerUsuarioAutenticadoId();
        verify(logService).registrarLog(eq(100), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-PROD-002: Crear producto sin usuario autenticado")
    void testCrearProducto_SinUsuarioAutenticado() {
        // Arrange
        when(productoMapper.toEntity(productoDTO)).thenReturn(producto);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(productoMapper.toDTO(producto)).thenReturn(productoDTO);
        when(usuarioHelper.obtenerUsuarioAutenticadoId()).thenReturn(null);

        // Act
        ProductoDTO result = productoService.crearProducto(productoDTO);

        // Assert
        assertThat(result).isNotNull();
        
        verify(productoRepository).save(any(Producto.class));
        verify(usuarioHelper).obtenerUsuarioAutenticadoId();
        verify(logService, never()).registrarLog(anyInt(), anyString());
    }

    // ==================== ACTUALIZAR PRODUCTO ====================

    @Test
    @DisplayName("TC-UNIT-PROD-003: Actualizar producto exitosamente")
    void testActualizarProducto_Success() {
        // Arrange
        ProductoDTO actualizacionDTO = new ProductoDTO();
        actualizacionDTO.setNombreProducto("Caja de Cartón Mediana");
        actualizacionDTO.setPrecioUnitario(new BigDecimal("4000.00"));

        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        doNothing().when(productoMapper).updateFromDTO(any(Producto.class), any(ProductoDTO.class));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(productoMapper.toDTO(producto)).thenReturn(productoDTO);
        when(usuarioHelper.obtenerUsuarioAutenticadoId()).thenReturn(100);
        doNothing().when(logService).registrarLog(anyInt(), anyString());

        // Act
        ProductoDTO result = productoService.actualizarProducto(1, actualizacionDTO);

        // Assert
        assertThat(result).isNotNull();
        
        verify(productoRepository).findById(1);
        verify(productoMapper).updateFromDTO(producto, actualizacionDTO);
        verify(productoRepository).save(producto);
        verify(logService).registrarLog(eq(100), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-PROD-004: Actualizar producto inexistente")
    void testActualizarProducto_NoExiste() {
        // Arrange
        when(productoRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productoService.actualizarProducto(999, productoDTO))
                .isInstanceOf(ProductoBusinessException.class)
                .hasMessageContaining("999");

        verify(productoRepository).findById(999);
        verify(productoRepository, never()).save(any());
    }

    // ==================== ELIMINAR PRODUCTO ====================

    @Test
    @DisplayName("TC-UNIT-PROD-005: Eliminar producto (soft delete) exitosamente")
    void testEliminarProducto_Success() {
        // Arrange
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(usuarioHelper.obtenerUsuarioAutenticadoId()).thenReturn(100);
        doNothing().when(logService).registrarLog(anyInt(), anyString());

        // Act
        productoService.eliminarProducto(1);

        // Assert
        verify(productoRepository).findById(1);
        verify(productoRepository).save(producto);
        assertThat(producto.getEstadoProducto()).isEqualTo("INACTIVO");
        verify(logService).registrarLog(eq(100), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-PROD-006: Eliminar producto inexistente")
    void testEliminarProducto_NoExiste() {
        // Arrange
        when(productoRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productoService.eliminarProducto(999))
                .isInstanceOf(ProductoBusinessException.class)
                .hasMessageContaining("999");

        verify(productoRepository).findById(999);
        verify(productoRepository, never()).save(any());
    }

    // ==================== LISTAR PRODUCTOS ====================

    @Test
    @DisplayName("TC-UNIT-PROD-007: Listar todos los productos activos")
    void testListarProductos_Success() {
        // Arrange
        Producto producto2 = new Producto();
        producto2.setIdProducto(2);
        producto2.setNombreProducto("Cinta de Embalaje");
        producto2.setEstadoProducto("ACTIVO");

        Producto producto3 = new Producto();
        producto3.setIdProducto(3);
        producto3.setNombreProducto("Producto Inactivo");
        producto3.setEstadoProducto("INACTIVO");

        List<Producto> productos = Arrays.asList(producto, producto2, producto3);

        when(productoRepository.findAll()).thenReturn(productos);
        when(productoMapper.toDTO(producto)).thenReturn(productoDTO);
        
        ProductoDTO dto2 = new ProductoDTO();
        dto2.setIdProducto(2);
        dto2.setNombreProducto("Cinta de Embalaje");
        when(productoMapper.toDTO(producto2)).thenReturn(dto2);

        // Act
        List<ProductoDTO> result = productoService.listarProductos();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // Solo activos
        assertThat(result).extracting(ProductoDTO::getNombreProducto)
                .contains("Caja de Cartón Grande", "Cinta de Embalaje");

        verify(productoRepository).findAll();
        verify(productoMapper, times(2)).toDTO(any(Producto.class));
    }

    @Test
    @DisplayName("TC-UNIT-PROD-008: Listar productos cuando no hay ninguno")
    void testListarProductos_Vacio() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ProductoDTO> result = productoService.listarProductos();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(productoRepository).findAll();
        verify(productoMapper, never()).toDTO(any());
    }

    // ==================== OBTENER POR ID ====================

    @Test
    @DisplayName("TC-UNIT-PROD-009: Obtener producto por ID exitosamente")
    void testObtenerProductoPorId_Success() {
        // Arrange
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(productoMapper.toDTO(producto)).thenReturn(productoDTO);

        // Act
        ProductoDTO result = productoService.obtenerProductoPorId(1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIdProducto()).isEqualTo(1);
        assertThat(result.getNombreProducto()).isEqualTo("Caja de Cartón Grande");

        verify(productoRepository).findById(1);
        verify(productoMapper).toDTO(producto);
    }

    @Test
    @DisplayName("TC-UNIT-PROD-010: Obtener producto por ID inexistente")
    void testObtenerProductoPorId_NoExiste() {
        // Arrange
        when(productoRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productoService.obtenerProductoPorId(999))
                .isInstanceOf(ProductoBusinessException.class)
                .hasMessageContaining("999");

        verify(productoRepository).findById(999);
        verify(productoMapper, never()).toDTO(any());
    }

    // ==================== BUSCAR POR NOMBRE ====================

    @Test
    @DisplayName("TC-UNIT-PROD-011: Buscar producto por nombre exitosamente")
    void testBuscarProductoPorNombre_Success() {
        // Arrange
        String nombreBusqueda = "Caja de Cartón Grande";
        when(productoRepository.findByNombreProducto(nombreBusqueda))
                .thenReturn(Optional.of(producto));
        when(productoMapper.toDTO(producto)).thenReturn(productoDTO);

        // Act
        ProductoDTO result = productoService.buscarProductoPorNombre(nombreBusqueda);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombreProducto()).isEqualTo(nombreBusqueda);

        verify(productoRepository).findByNombreProducto(nombreBusqueda);
        verify(productoMapper).toDTO(producto);
    }

    @Test
    @DisplayName("TC-UNIT-PROD-012: Buscar producto por nombre inexistente")
    void testBuscarProductoPorNombre_NoExiste() {
        // Arrange
        String nombreInexistente = "Producto que no existe";
        when(productoRepository.findByNombreProducto(nombreInexistente))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productoService.buscarProductoPorNombre(nombreInexistente))
                .isInstanceOf(ProductoBusinessException.class)
                .hasMessageContaining(nombreInexistente);

        verify(productoRepository).findByNombreProducto(nombreInexistente);
        verify(productoMapper, never()).toDTO(any());
    }

    // ==================== VALIDACIONES ====================

    @Test
    @DisplayName("TC-UNIT-PROD-013: Validar que mapper convierte correctamente")
    void testMapper_Conversiones() {
        // Arrange
        when(productoMapper.toEntity(productoDTO)).thenReturn(producto);
        when(productoMapper.toDTO(producto)).thenReturn(productoDTO);

        // Act
        Producto entity = productoMapper.toEntity(productoDTO);
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(dto).isNotNull();

        verify(productoMapper).toEntity(productoDTO);
        verify(productoMapper).toDTO(producto);
    }
}
