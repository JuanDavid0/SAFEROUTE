package com.saferoute.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.saferoute.dto.EtiquetaDTO;
import com.saferoute.dto.EtiquetasResponseDTO;
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

    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DeviceRgb COLOR_SAFEROUTE = new DeviceRgb(228, 107, 107);

    @Override
    @Transactional(readOnly = true)
    public EtiquetasResponseDTO generarEtiquetasJSON(Integer idPedido) {
        log.info("🏷️ Generando etiquetas JSON para pedido ID: {}", idPedido);

        // Validar pedido
        Pedido pedido = validarPedido(idPedido);

        // Obtener solicitudes del pedido
        List<Solicitud> solicitudes = solicitudRepository.findByPedido(pedido);

        if (solicitudes.isEmpty()) {
            throw new RuntimeException("El pedido no tiene solicitudes");
        }

        // Generar etiquetas
        List<EtiquetaDTO> etiquetas = new ArrayList<>();
        int numeroEtiqueta = 1;
        int totalEtiquetas = solicitudes.size();

        for (Solicitud solicitud : solicitudes) {
            EtiquetaDTO etiqueta = EtiquetaDTO.builder()
                    .idSolicitud(solicitud.getIdSolicitud())
                    .idPedido(idPedido)
                    .nombreCliente(solicitud.getCliente().getNombres() + " " + solicitud.getCliente().getApellidos())
                    .direccion(solicitud.getDireccionEntrega())
                    .telefono(solicitud.getCliente().getTelefono())
                    .numeroEtiqueta(numeroEtiqueta++)
                    .totalEtiquetas(totalEtiquetas)
                    .build();

            etiquetas.add(etiqueta);
        }

        log.info("✅ Generadas {} etiquetas para pedido #{}", totalEtiquetas, idPedido);

        return EtiquetasResponseDTO.builder()
                .idPedido(idPedido)
                .fechaEntrega(pedido.getFechaCreado().format(FECHA_FORMATTER))
                .etiquetas(etiquetas)
                .totalEtiquetas(totalEtiquetas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarEtiquetasPDF(Integer idPedido) throws Exception {
        log.info("📄 Generando PDF de etiquetas para pedido ID: {}", idPedido);

        // Obtener etiquetas
        EtiquetasResponseDTO etiquetasData = generarEtiquetasJSON(idPedido);
        List<EtiquetaDTO> etiquetas = etiquetasData.getEtiquetas();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.LETTER);

            // Márgenes más pequeños para aprovechar espacio
            document.setMargins(20, 20, 20, 20);

            // Título del documento
            Paragraph titulo = new Paragraph("ETIQUETAS DE ENTREGA - PEDIDO #" + idPedido)
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(titulo);

            // Crear tabla con 2 columnas (2 etiquetas por fila)
            float[] columnWidths = { 1, 1 };
            Table mainTable = new Table(UnitValue.createPercentArray(columnWidths));
            mainTable.setWidth(UnitValue.createPercentValue(100));

            // Generar etiquetas en cuadrícula
            for (int i = 0; i < etiquetas.size(); i++) {
                EtiquetaDTO etiqueta = etiquetas.get(i);

                // Crear celda de etiqueta
                Cell etiquetaCell = crearEtiqueta(etiqueta);
                mainTable.addCell(etiquetaCell);

                // Si es la última etiqueta y es impar, agregar celda vacía
                if (i == etiquetas.size() - 1 && etiquetas.size() % 2 != 0) {
                    mainTable.addCell(new Cell().setBorder(null));
                }
            }

            document.add(mainTable);

            // Pie de página
            Paragraph footer = new Paragraph("Total de etiquetas: " + etiquetas.size())
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10);
            document.add(footer);

            document.close();

            log.info("✅ PDF de etiquetas generado exitosamente - {} bytes", outputStream.size());
            return outputStream;

        } catch (Exception e) {
            log.error("❌ Error al generar PDF de etiquetas: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Crea una celda de etiqueta individual con diseño profesional
     */
    private Cell crearEtiqueta(EtiquetaDTO etiqueta) {
        Cell cell = new Cell();
        cell.setPadding(10);
        cell.setMargin(5);
        cell.setBorder(new SolidBorder(ColorConstants.BLACK, 1));
        cell.setMinHeight(120);
        cell.setVerticalAlignment(VerticalAlignment.TOP);

        // Logo/Título SafeRoute
        Paragraph saferoute = new Paragraph("🚚 SAFE ROUTE")
                .setFontSize(16)
                .setBold()
                .setFontColor(COLOR_SAFEROUTE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8);
        cell.add(saferoute);

        // Línea separadora
        Paragraph linea = new Paragraph("━━━━━━━━━━━━━━━━━━━━━━")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8);
        cell.add(linea);

        // Cliente
        Paragraph cliente = new Paragraph()
                .add(new Paragraph("Cliente:").setBold().setFontSize(11))
                .add(new Paragraph(etiqueta.getNombreCliente()).setFontSize(12))
                .setMarginBottom(5);
        cell.add(cliente);

        // Dirección
        Paragraph direccion = new Paragraph()
                .add(new Paragraph("Dirección:").setBold().setFontSize(11))
                .add(new Paragraph(etiqueta.getDireccion()).setFontSize(12))
                .setMarginBottom(5);
        cell.add(direccion);

        // Contacto
        Paragraph contacto = new Paragraph()
                .add(new Paragraph("Contacto:").setBold().setFontSize(11))
                .add(new Paragraph(etiqueta.getTelefono()).setFontSize(12))
                .setMarginBottom(8);
        cell.add(contacto);

        return cell;
    }

    /**
     * Valida que el pedido exista y esté en estado ENTREGADO
     */
    private Pedido validarPedido(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstadoPedido() != EstadoPedidoEnum.ENT) {
            log.warn("⚠️ Pedido {} no está en estado ENTREGADO (estado actual: {})",
                    idPedido, pedido.getEstadoPedido());
            throw new RuntimeException("Solo se pueden generar etiquetas para pedidos entregados (estado ENT)");
        }

        return pedido;
    }
}
