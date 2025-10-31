package com.saferoute.service;

import com.itextpdf.layout.element.Cell;
import com.saferoute.dto.EtiquetaDTO;
import com.saferoute.dto.EtiquetasResponseDTO;
import com.saferoute.exception.EtiquetaBusinessException;
import com.saferoute.helper.EtiquetaPdfHelper;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.Usuario;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.repository.PedidoRepository;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.service.impl.EtiquetaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Etiqueta Service")
class EtiquetaServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private EtiquetaPdfHelper pdfHelper;

    @InjectMocks
    private EtiquetaServiceImpl etiquetaService;

    private Pedido pedido;
    private Solicitud solicitud;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        cliente = new Usuario();
        cliente.setIdUsuario(10);
        cliente.setNombres("Ana");
        cliente.setApellidos("Gomez");
        cliente.setTelefono("3001234567");

        pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setFechaCreado(LocalDate.of(2025,10,20));
        pedido.setEstadoPedido(EstadoPedidoEnum.ENT);

        solicitud = new Solicitud();
        solicitud.setIdSolicitud(100);
        solicitud.setCliente(cliente);
        solicitud.setDireccionEntrega("Calle 123");
    }

    @Test
    @DisplayName("TC-UNIT-ETIQUETA-01: Generar etiquetas JSON exitoso")
    void testGenerarEtiquetasJSON_Exitoso() {
        // Arrange
        List<Solicitud> solicitudes = Collections.singletonList(solicitud);
        when(pedidoRepository.findById(1)).thenReturn(java.util.Optional.of(pedido));
        when(solicitudRepository.findByPedido(pedido)).thenReturn(solicitudes);

        // Act
        EtiquetasResponseDTO resultado = etiquetaService.generarEtiquetasJSON(1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPedido()).isEqualTo(1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        assertThat(resultado.getFechaEntrega()).isEqualTo(pedido.getFechaCreado().format(fmt));
        assertThat(resultado.getEtiquetas()).hasSize(1);

        EtiquetaDTO dto = resultado.getEtiquetas().get(0);
        assertThat(dto.getIdSolicitud()).isEqualTo(100);
        assertThat(dto.getIdPedido()).isEqualTo(1);
        assertThat(dto.getNombreCliente()).contains("Ana").contains("Gomez");
        assertThat(dto.getDireccion()).isEqualTo("Calle 123");
        assertThat(dto.getTelefono()).isEqualTo("3001234567");
        assertThat(resultado.getTotalEtiquetas()).isEqualTo(1);

        verify(pedidoRepository).findById(1);
        verify(solicitudRepository).findByPedido(pedido);
    }

    @Test
    @DisplayName("TC-UNIT-ETIQUETA-02: Error - pedido no encontrado")
    void testGenerarEtiquetasJSON_PedidoNoEncontrado() {
        when(pedidoRepository.findById(999)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> etiquetaService.generarEtiquetasJSON(999))
                .isInstanceOf(EtiquetaBusinessException.class)
                .hasMessageContaining("no encontrado");

        verify(pedidoRepository).findById(999);
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    @DisplayName("TC-UNIT-ETIQUETA-03: Error - pedido no entregado")
    void testGenerarEtiquetasJSON_PedidoNoEntregado() {
        pedido.setEstadoPedido(EstadoPedidoEnum.ACT);
        when(pedidoRepository.findById(1)).thenReturn(java.util.Optional.of(pedido));

    assertThatThrownBy(() -> etiquetaService.generarEtiquetasJSON(1))
        .isInstanceOf(EtiquetaBusinessException.class)
        .hasMessageContaining("entregados");

        verify(pedidoRepository).findById(1);
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    @DisplayName("TC-UNIT-ETIQUETA-04: Error - pedido sin solicitudes")
    void testGenerarEtiquetasJSON_SinSolicitudes() {
        when(pedidoRepository.findById(1)).thenReturn(java.util.Optional.of(pedido));
        when(solicitudRepository.findByPedido(pedido)).thenReturn(new ArrayList<>());

    assertThatThrownBy(() -> etiquetaService.generarEtiquetasJSON(1))
        .isInstanceOf(EtiquetaBusinessException.class)
        .hasMessageContaining("no tiene solicitudes");

        verify(pedidoRepository).findById(1);
        verify(solicitudRepository).findByPedido(pedido);
    }

    @Test
    @DisplayName("TC-UNIT-ETIQUETA-05: Generar PDF de etiquetas exitoso")
    void testGenerarEtiquetasPDF_Exitoso() throws Exception {
        // Arrange
        List<Solicitud> solicitudes = Collections.singletonList(solicitud);
        when(pedidoRepository.findById(1)).thenReturn(java.util.Optional.of(pedido));
        when(solicitudRepository.findByPedido(pedido)).thenReturn(solicitudes);
        when(pdfHelper.crearCeldaEtiqueta(any())).thenReturn(new Cell());
        when(pdfHelper.crearCeldaVacia()).thenReturn(new Cell());

        // Act
        ByteArrayOutputStream stream = etiquetaService.generarEtiquetasPDF(1);

        // Assert
        assertThat(stream).isNotNull();
        assertThat(stream.size()).isGreaterThanOrEqualTo(0);

        verify(pedidoRepository).findById(1);
        verify(solicitudRepository).findByPedido(pedido);
        verify(pdfHelper, atLeastOnce()).crearCeldaEtiqueta(any());
    }
}
