package com.saferoute.service.impl;

import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.interfaces.IExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 📊 IMPLEMENTACIÓN DEL SERVICIO DE EXPORTACIÓN A EXCEL
 * 
 * Genera informes contables profesionales en formato .xlsx con:
 * - Formato de tabla específico
 * - Estilos y colores corporativos
 * - Datos ordenados y consolidados
 * - Headers claros y totales
 * 
 * @author SafeRoute Team
 * @version 2.0
 * @since 2025-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExcelServiceImpl implements IExcelService {

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final SolicitudRepository solicitudRepository;

    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarInformeCliente(Integer idCliente) throws Exception {
        log.info("📄 Generando informe de compras para cliente ID: {}", idCliente);

        // Obtener cliente
        Usuario cliente = usuarioRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Obtener solicitudes pagadas del cliente CON productos cargados
        List<Solicitud> solicitudes = solicitudRepository.findByClienteWithProductos(cliente).stream()
                .filter(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.PGD)
                .sorted(Comparator.comparing(Solicitud::getFechaSolicitud).reversed())
                .collect(Collectors.toList());

        if (solicitudes.isEmpty()) {
            log.warn("⚠️ Cliente {} no tiene solicitudes pagadas", idCliente);
            throw new RuntimeException("El cliente no tiene compras registradas");
        }

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Compras Cliente");

            // Crear estilos
            CellStyle tituloStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle normalStyle = crearEstiloNormal(workbook);
            CellStyle monedaStyle = crearEstiloMoneda(workbook);
            CellStyle fechaStyle = crearEstiloFecha(workbook);
            CellStyle totalStyle = crearEstiloTotal(workbook);

            int rowNum = 0;

            // Título
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            String nombreCompleto = cliente.getNombres() + " " + cliente.getApellidos();
            titleCell.setCellValue("📊 INFORME DE COMPRAS - " + nombreCompleto);
            titleCell.setCellStyle(tituloStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            rowNum++;

            // Info del cliente
            Row infoRow = sheet.createRow(rowNum++);
            Cell infoCell = infoRow.createCell(0);
            infoCell.setCellValue("Cliente: " + nombreCompleto + " | Cédula: " + cliente.getCedula());
            infoCell.setCellStyle(normalStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));
            rowNum++;

            // Headers de la tabla
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = { "Fecha", "Producto", "Cantidad", "Precio Unitario", "Precio Total" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            BigDecimal totalGeneral = BigDecimal.ZERO;

            for (Solicitud solicitud : solicitudes) {
                for (SolicitudProducto sp : solicitud.getProductos()) {
                    Row row = sheet.createRow(rowNum++);

                    BigDecimal precioTotal = sp.getPrecio().multiply(BigDecimal.valueOf(sp.getCantidadSolicitada()));
                    totalGeneral = totalGeneral.add(precioTotal);

                    // Fecha
                    Cell fechaCell = row.createCell(0);
                    fechaCell.setCellValue(solicitud.getFechaSolicitud().format(FECHA_FORMATTER));
                    fechaCell.setCellStyle(fechaStyle);

                    // Producto
                    Cell productoCell = row.createCell(1);
                    productoCell.setCellValue(sp.getProducto().getNombreProducto());
                    productoCell.setCellStyle(normalStyle);

                    // Cantidad
                    Cell cantidadCell = row.createCell(2);
                    cantidadCell.setCellValue(sp.getCantidadSolicitada());
                    cantidadCell.setCellStyle(normalStyle);

                    // Precio Unitario
                    Cell precioUnitCell = row.createCell(3);
                    precioUnitCell.setCellValue(sp.getPrecio().doubleValue());
                    precioUnitCell.setCellStyle(monedaStyle);

                    // Precio Total
                    Cell precioTotalCell = row.createCell(4);
                    precioTotalCell.setCellValue(precioTotal.doubleValue());
                    precioTotalCell.setCellStyle(monedaStyle);
                }
            }

            // Fila de total
            rowNum++;
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(3);
            totalLabelCell.setCellValue("TOTAL:");
            totalLabelCell.setCellStyle(totalStyle);

            Cell totalValueCell = totalRow.createCell(4);
            totalValueCell.setCellValue(totalGeneral.doubleValue());
            totalValueCell.setCellStyle(totalStyle);

            // Ajustar anchos de columna
            sheet.setColumnWidth(0, 3500); // Fecha
            sheet.setColumnWidth(1, 8000); // Producto
            sheet.setColumnWidth(2, 3000); // Cantidad
            sheet.setColumnWidth(3, 4500); // Precio Unitario
            sheet.setColumnWidth(4, 4500); // Precio Total

            workbook.write(outputStream);
            log.info("✅ Informe de cliente {} generado exitosamente", idCliente);
            return outputStream;

        } catch (Exception e) {
            log.error("❌ Error al generar informe de cliente {}: {}", idCliente, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarInformePedidoConsolidado(Integer idPedido) throws Exception {
        log.info("📊 Generando informe consolidado para pedido ID: {}", idPedido);

        // Obtener pedido
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Validar que el pedido esté entregado
        if (pedido.getEstadoPedido() != EstadoPedidoEnum.ENT) {
            log.warn("⚠️ Pedido {} no está en estado ENTREGADO (estado actual: {})",
                    idPedido, pedido.getEstadoPedido());
            throw new RuntimeException("Solo se pueden generar informes de pedidos entregados (estado ENT)");
        }

        // Obtener solicitudes pagadas del pedido CON productos cargados
        List<Solicitud> solicitudes = solicitudRepository.findByPedidoWithProductos(pedido).stream()
                .filter(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.PGD)
                .collect(Collectors.toList());

        if (solicitudes.isEmpty()) {
            log.warn("⚠️ Pedido {} no tiene solicitudes pagadas", idPedido);
            throw new RuntimeException("El pedido no tiene solicitudes pagadas");
        }

        // Consolidar productos (sumar cantidades del mismo producto)
        Map<Integer, ProductoConsolidado> productosConsolidados = new HashMap<>();

        for (Solicitud solicitud : solicitudes) {
            for (SolicitudProducto sp : solicitud.getProductos()) {
                Integer idProducto = sp.getProducto().getIdProducto();
                ProductoConsolidado consolidado = productosConsolidados.get(idProducto);

                if (consolidado == null) {
                    consolidado = new ProductoConsolidado();
                    consolidado.codigoProducto = idProducto;
                    consolidado.nombreProducto = sp.getProducto().getNombreProducto();
                    consolidado.precio = sp.getPrecio();
                    consolidado.cantidad = 0;
                    consolidado.fechaPrimera = solicitud.getFechaSolicitud();
                    productosConsolidados.put(idProducto, consolidado);
                }

                consolidado.cantidad += sp.getCantidadSolicitada();

                // Actualizar fecha si es más reciente
                if (solicitud.getFechaSolicitud().isAfter(consolidado.fechaPrimera)) {
                    consolidado.fechaPrimera = solicitud.getFechaSolicitud();
                }
            }
        }

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Informe Pedido");

            // Crear estilos
            CellStyle tituloStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle normalStyle = crearEstiloNormal(workbook);
            CellStyle monedaStyle = crearEstiloMoneda(workbook);
            CellStyle fechaStyle = crearEstiloFecha(workbook);
            CellStyle totalStyle = crearEstiloTotal(workbook);

            int rowNum = 0;

            // Título
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("📊 INFORME DE PEDIDO #" + idPedido);
            titleCell.setCellStyle(tituloStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            rowNum++;

            // Info del pedido
            Row infoRow = sheet.createRow(rowNum++);
            Cell infoCell = infoRow.createCell(0);
            infoCell.setCellValue("Pedido: #" + idPedido + " | Estado: ENTREGADO | Fecha: " +
                    pedido.getFechaCreado().format(FECHA_FORMATTER));
            infoCell.setCellStyle(normalStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));
            rowNum++;

            // Headers de la tabla
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = { "Código Producto", "Fecha", "Nombre Producto", "Cantidad", "Precio Unitario",
                    "Precio Total" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Ordenar productos por código
            List<ProductoConsolidado> productosOrdenados = productosConsolidados.values().stream()
                    .sorted(Comparator.comparing(p -> p.codigoProducto))
                    .collect(Collectors.toList());

            // Datos
            BigDecimal totalGeneral = BigDecimal.ZERO;

            for (ProductoConsolidado producto : productosOrdenados) {
                Row row = sheet.createRow(rowNum++);

                BigDecimal precioTotal = producto.precio.multiply(BigDecimal.valueOf(producto.cantidad));
                totalGeneral = totalGeneral.add(precioTotal);

                // Código Producto
                Cell codigoCell = row.createCell(0);
                codigoCell.setCellValue(producto.codigoProducto);
                codigoCell.setCellStyle(normalStyle);

                // Fecha
                Cell fechaCell = row.createCell(1);
                fechaCell.setCellValue(producto.fechaPrimera.format(FECHA_FORMATTER));
                fechaCell.setCellStyle(fechaStyle);

                // Nombre Producto
                Cell nombreCell = row.createCell(2);
                nombreCell.setCellValue(producto.nombreProducto);
                nombreCell.setCellStyle(normalStyle);

                // Cantidad
                Cell cantidadCell = row.createCell(3);
                cantidadCell.setCellValue(producto.cantidad);
                cantidadCell.setCellStyle(normalStyle);

                // Precio Unitario
                Cell precioUnitCell = row.createCell(4);
                precioUnitCell.setCellValue(producto.precio.doubleValue());
                precioUnitCell.setCellStyle(monedaStyle);

                // Precio Total
                Cell precioTotalCell = row.createCell(5);
                precioTotalCell.setCellValue(precioTotal.doubleValue());
                precioTotalCell.setCellStyle(monedaStyle);
            }

            // Fila de total
            rowNum++;
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(4);
            totalLabelCell.setCellValue("TOTAL:");
            totalLabelCell.setCellStyle(totalStyle);

            Cell totalValueCell = totalRow.createCell(5);
            totalValueCell.setCellValue(totalGeneral.doubleValue());
            totalValueCell.setCellStyle(totalStyle);

            // Ajustar anchos de columna
            sheet.setColumnWidth(0, 4000); // Código
            sheet.setColumnWidth(1, 3500); // Fecha
            sheet.setColumnWidth(2, 8000); // Nombre Producto
            sheet.setColumnWidth(3, 3000); // Cantidad
            sheet.setColumnWidth(4, 4500); // Precio Unitario
            sheet.setColumnWidth(5, 4500); // Precio Total

            workbook.write(outputStream);
            log.info("✅ Informe de pedido {} generado exitosamente", idPedido);
            return outputStream;

        } catch (Exception e) {
            log.error("❌ Error al generar informe de pedido {}: {}", idPedido, e.getMessage());
            throw e;
        }
    }

    // ========== ESTILOS ==========

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloNormal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = crearEstiloNormal(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = crearEstiloNormal(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloTotal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.DOUBLE);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    // ========== CLASE AUXILIAR ==========

    private static class ProductoConsolidado {
        Integer codigoProducto;
        String nombreProducto;
        int cantidad;
        BigDecimal precio;
        LocalDate fechaPrimera;
    }
}
