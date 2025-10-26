package com.saferoute.service.impl;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.saferoute.constants.EtiquetaConstants;
import com.saferoute.dto.EtiquetaDTO;
import com.saferoute.dto.EtiquetasResponseDTO;
import com.saferoute.exception.EtiquetaBusinessException;
import com.saferoute.helper.EtiquetaPdfHelper;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.repository.PedidoRepository;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.service.interfaces.IEtiquetaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 🏷️ IMPLEMENTACIÓN DEL SERVICIO DE ETIQUETAS
 * 
 * Genera etiquetas de entrega para pedidos entregados.
 * Cada solicitud del pedido genera una etiqueta independiente.
 * 
 * Formato PDF:
 * - 2 etiquetas por fila (cuadrícula optimizada)
 * - Diseño profesional con bordes
 * - Información clara y legible
 * - Lista para cortar e imprimir
 * 
 * @author SafeRoute Team
 * @version 1.0
 * @since 2025-10-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtiquetaServiceImpl implements IEtiquetaService {

    private final PedidoRepository pedidoRepository;
    private final SolicitudRepository solicitudRepository;
    private final EtiquetaPdfHelper pdfHelper;

    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter
            .ofPattern(EtiquetaConstants.FORMATO_FECHA);

    @Override
    @Transactional(readOnly = true)
    public EtiquetasResponseDTO generarEtiquetasJSON(Integer idPedido) {
        log.info(EtiquetaConstants.LOG_GENERANDO_ETIQUETAS_JSON, idPedido);

        Pedido pedido = validarPedido(idPedido);
        List<Solicitud> solicitudes = obtenerSolicitudesPedido(pedido);
        List<EtiquetaDTO> etiquetas = construirEtiquetas(solicitudes, idPedido);

        log.info(EtiquetaConstants.LOG_ETIQUETAS_GENERADAS, etiquetas.size(), idPedido);

        return EtiquetasResponseDTO.builder()
                .idPedido(idPedido)
                .fechaEntrega(pedido.getFechaCreado().format(FECHA_FORMATTER))
                .etiquetas(etiquetas)
                .totalEtiquetas(etiquetas.size())
                .build();
    }

    private List<Solicitud> obtenerSolicitudesPedido(Pedido pedido) {
        List<Solicitud> solicitudes = solicitudRepository.findByPedido(pedido);

        if (solicitudes.isEmpty()) {
            throw new EtiquetaBusinessException(EtiquetaConstants.ERROR_PEDIDO_SIN_SOLICITUDES);
        }

        return solicitudes;
    }

    private List<EtiquetaDTO> construirEtiquetas(List<Solicitud> solicitudes, Integer idPedido) {
        List<EtiquetaDTO> etiquetas = new ArrayList<>();
        int numeroEtiqueta = 1;
        int totalEtiquetas = solicitudes.size();

        for (Solicitud solicitud : solicitudes) {
            EtiquetaDTO etiqueta = crearEtiquetaDTO(solicitud, idPedido, numeroEtiqueta++, totalEtiquetas);
            etiquetas.add(etiqueta);
        }

        return etiquetas;
    }

    private EtiquetaDTO crearEtiquetaDTO(Solicitud solicitud, Integer idPedido,
            int numeroEtiqueta, int totalEtiquetas) {
        return EtiquetaDTO.builder()
                .idSolicitud(solicitud.getIdSolicitud())
                .idPedido(idPedido)
                .nombreCliente(construirNombreCompleto(solicitud))
                .direccion(solicitud.getDireccionEntrega())
                .telefono(solicitud.getCliente().getTelefono())
                .numeroEtiqueta(numeroEtiqueta)
                .totalEtiquetas(totalEtiquetas)
                .build();
    }

    private String construirNombreCompleto(Solicitud solicitud) {
        String nombres = solicitud.getCliente().getNombres();
        String apellidos = solicitud.getCliente().getApellidos();

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

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarEtiquetasPDF(Integer idPedido) {
        log.info(EtiquetaConstants.LOG_GENERANDO_ETIQUETAS_PDF, idPedido);

        EtiquetasResponseDTO etiquetasData = generarEtiquetasJSON(idPedido);
        List<EtiquetaDTO> etiquetas = etiquetasData.getEtiquetas();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = crearDocumentoPDF(outputStream, idPedido);
            Table mainTable = crearTablaEtiquetas(etiquetas);

            document.add(mainTable);
            document.add(crearFooter(etiquetas.size()));
            document.close();

            log.info(EtiquetaConstants.LOG_PDF_GENERADO, outputStream.size());
            return outputStream;

        } catch (Exception e) {
            log.error(EtiquetaConstants.LOG_ERROR_GENERANDO_PDF, e.getMessage());
            throw new EtiquetaBusinessException(
                    String.format("Error al generar PDF de etiquetas del pedido %d: %s", idPedido, e.getMessage()), e);
        }
    }

    private Document crearDocumentoPDF(ByteArrayOutputStream outputStream, Integer idPedido) {
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.LETTER);

        document.setMargins(EtiquetaConstants.MARGEN_DOCUMENTO,
                EtiquetaConstants.MARGEN_DOCUMENTO,
                EtiquetaConstants.MARGEN_DOCUMENTO,
                EtiquetaConstants.MARGEN_DOCUMENTO);

        document.add(crearTituloDocumento(idPedido));
        return document;
    }

    private Paragraph crearTituloDocumento(Integer idPedido) {
        return new Paragraph(EtiquetaConstants.TITULO_ETIQUETAS_ENTREGA + idPedido)
                .setFontSize(EtiquetaConstants.FONT_SIZE_TITULO_DOC)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(EtiquetaConstants.MARGEN_BOTTOM_TITULO);
    }

    private Table crearTablaEtiquetas(List<EtiquetaDTO> etiquetas) {
        float[] columnWidths = new float[EtiquetaConstants.COLUMNAS_POR_FILA];
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] = 1;
        }

        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        agregarEtiquetasATabla(table, etiquetas);

        return table;
    }

    private void agregarEtiquetasATabla(Table table, List<EtiquetaDTO> etiquetas) {
        for (int i = 0; i < etiquetas.size(); i++) {
            Cell etiquetaCell = pdfHelper.crearCeldaEtiqueta(etiquetas.get(i));
            table.addCell(etiquetaCell);

            // Si es la última etiqueta y es impar, agregar celda vacía
            if (i == etiquetas.size() - 1 && etiquetas.size() % EtiquetaConstants.COLUMNAS_POR_FILA != 0) {
                table.addCell(pdfHelper.crearCeldaVacia());
            }
        }
    }

    private Paragraph crearFooter(int totalEtiquetas) {
        return new Paragraph(EtiquetaConstants.FOOTER_TOTAL_ETIQUETAS + totalEtiquetas)
                .setFontSize(EtiquetaConstants.FONT_SIZE_FOOTER)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(EtiquetaConstants.MARGEN_TOP_FOOTER);
    }

    /**
     * Valida que el pedido exista y esté en estado ENTREGADO
     */
    private Pedido validarPedido(Integer idPedido) {
        log.debug("Validando pedido ID: {}", idPedido);

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> {
                    log.error("❌ Pedido no encontrado ID: {}", idPedido);
                    return new EtiquetaBusinessException(EtiquetaConstants.ERROR_PEDIDO_NO_ENCONTRADO);
                });

        if (pedido.getEstadoPedido() != EstadoPedidoEnum.ENT) {
            log.warn(EtiquetaConstants.LOG_PEDIDO_NO_ENTREGADO, idPedido, pedido.getEstadoPedido());
            throw new EtiquetaBusinessException(EtiquetaConstants.ERROR_PEDIDO_NO_ENTREGADO);
        }

        return pedido;
    }
}
