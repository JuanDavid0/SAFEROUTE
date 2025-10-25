package com.saferoute.service.impl;

import com.saferoute.constants.ExcelConstants;
import com.saferoute.exception.ExcelBusinessException;
import com.saferoute.helper.ExcelRowHelper;
import com.saferoute.helper.ExcelStyleHelper;
import com.saferoute.helper.ProductoConsolidacionHelper;
import com.saferoute.helper.ProductoConsolidacionHelper.ProductoConsolidado;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.interfaces.IExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
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
 * IMPLEMENTACIÓN DEL SERVICIO DE EXPORTACIÓN A EXCEL
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
    private final ExcelStyleHelper styleHelper;
    private final ExcelRowHelper rowHelper;
    private final ProductoConsolidacionHelper consolidacionHelper;

    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern(ExcelConstants.FORMATO_FECHA);

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarInformeCliente(Integer idCliente) {
        log.info(ExcelConstants.LOG_GENERANDO_INFORME_CLIENTE, idCliente);

        Usuario cliente = obtenerCliente(idCliente);
        List<Solicitud> solicitudes = obtenerSolicitudesPagadasCliente(cliente, idCliente);

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(ExcelConstants.NOMBRE_HOJA_CLIENTE);
            Map<String, CellStyle> estilos = crearMapaEstilos(workbook);

            int rowNum = construirInformeCliente(sheet, cliente, solicitudes, estilos);
            configurarAnchoColumnasCliente(sheet);

            workbook.write(outputStream);
            log.info(ExcelConstants.LOG_INFORME_CLIENTE_GENERADO, idCliente);
            return outputStream;

        } catch (Exception e) {
            log.error(ExcelConstants.LOG_ERROR_GENERANDO_INFORME_CLIENTE, idCliente, e.getMessage());
            throw new ExcelBusinessException(
                    String.format("Error al generar informe Excel del cliente %d: %s", idCliente, e.getMessage()), e);
        }
    }

    private Usuario obtenerCliente(Integer idCliente) {
        return usuarioRepository.findById(idCliente)
                .orElseThrow(() -> new ExcelBusinessException(ExcelConstants.ERROR_CLIENTE_NO_ENCONTRADO));
    }

    private List<Solicitud> obtenerSolicitudesPagadasCliente(Usuario cliente, Integer idCliente) {
        List<Solicitud> solicitudes = solicitudRepository.findByClienteWithProductos(cliente).stream()
                .filter(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.PGD)
                .sorted(Comparator.comparing(Solicitud::getFechaSolicitud).reversed())
                .collect(Collectors.toList());

        if (solicitudes.isEmpty()) {
            log.warn(ExcelConstants.LOG_CLIENTE_SIN_COMPRAS, idCliente);
            throw new ExcelBusinessException(ExcelConstants.ERROR_CLIENTE_SIN_COMPRAS);
        }

        return solicitudes;
    }

    private int construirInformeCliente(Sheet sheet, Usuario cliente,
            List<Solicitud> solicitudes,
            Map<String, CellStyle> estilos) {
        int rowNum = 0;

        // Título
        String nombreCompleto = cliente.getNombres() + " " + cliente.getApellidos();
        rowHelper.crearFilaTitulo(sheet, rowNum++,
                ExcelConstants.TITULO_INFORME_COMPRAS + nombreCompleto,
                ExcelConstants.HEADERS_INFORME_CLIENTE.length,
                estilos.get("titulo"));
        rowNum++;

        // Información del cliente
        String infoCliente = ExcelConstants.PREFIJO_INFO_CLIENTE + nombreCompleto +
                ExcelConstants.PREFIJO_INFO_CEDULA + cliente.getCedula();
        rowHelper.crearFilaInfo(sheet, rowNum++, infoCliente,
                ExcelConstants.HEADERS_INFORME_CLIENTE.length,
                estilos.get("normal"));
        rowNum++;

        // Headers
        rowHelper.crearFilaHeaders(sheet, rowNum++,
                ExcelConstants.HEADERS_INFORME_CLIENTE,
                estilos.get("header"));

        // Datos
        BigDecimal totalGeneral = agregarFilasSolicitudesCliente(sheet, solicitudes, rowNum, estilos);
        rowNum += contarFilasDatos(solicitudes);

        // Total
        rowNum++;
        rowHelper.crearFilaTotal(sheet, rowNum, 3, 4, totalGeneral, estilos.get("total"));

        return rowNum;
    }

    private BigDecimal agregarFilasSolicitudesCliente(Sheet sheet, List<Solicitud> solicitudes,
            int rowNum, Map<String, CellStyle> estilos) {
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (Solicitud solicitud : solicitudes) {
            for (SolicitudProducto sp : solicitud.getProductos()) {
                Row row = sheet.createRow(rowNum++);

                BigDecimal precioTotal = sp.getPrecio()
                        .multiply(BigDecimal.valueOf(sp.getCantidadSolicitada()));
                totalGeneral = totalGeneral.add(precioTotal);

                rowHelper.crearCeldaTexto(row, 0,
                        solicitud.getFechaSolicitud().format(FECHA_FORMATTER),
                        estilos.get("fecha"));
                rowHelper.crearCeldaTexto(row, 1,
                        sp.getProducto().getNombreProducto(),
                        estilos.get("normal"));
                rowHelper.crearCeldaNumerica(row, 2,
                        sp.getCantidadSolicitada(),
                        estilos.get("normal"));
                rowHelper.crearCeldaMoneda(row, 3,
                        sp.getPrecio(),
                        estilos.get("moneda"));
                rowHelper.crearCeldaMoneda(row, 4,
                        precioTotal,
                        estilos.get("moneda"));
            }
        }

        return totalGeneral;
    }

    private int contarFilasDatos(List<Solicitud> solicitudes) {
        return solicitudes.stream()
                .mapToInt(s -> s.getProductos().size())
                .sum();
    }

    private void configurarAnchoColumnasCliente(Sheet sheet) {
        sheet.setColumnWidth(0, ExcelConstants.ANCHO_COLUMNA_FECHA);
        sheet.setColumnWidth(1, ExcelConstants.ANCHO_COLUMNA_PRODUCTO);
        sheet.setColumnWidth(2, ExcelConstants.ANCHO_COLUMNA_CANTIDAD);
        sheet.setColumnWidth(3, ExcelConstants.ANCHO_COLUMNA_PRECIO);
        sheet.setColumnWidth(4, ExcelConstants.ANCHO_COLUMNA_PRECIO);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarInformePedidoConsolidado(Integer idPedido) {
        log.info(ExcelConstants.LOG_GENERANDO_INFORME_PEDIDO, idPedido);

        Pedido pedido = obtenerPedido(idPedido);
        validarPedidoEntregado(pedido, idPedido);
        List<Solicitud> solicitudes = obtenerSolicitudesPagadasPedido(pedido, idPedido);

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(ExcelConstants.NOMBRE_HOJA_PEDIDO);
            Map<String, CellStyle> estilos = crearMapaEstilos(workbook);

            int rowNum = construirInformePedido(sheet, pedido, solicitudes, estilos);
            configurarAnchoColumnasPedido(sheet);

            workbook.write(outputStream);
            log.info(ExcelConstants.LOG_INFORME_PEDIDO_GENERADO, idPedido);
            return outputStream;

        } catch (Exception e) {
            log.error(ExcelConstants.LOG_ERROR_GENERANDO_INFORME_PEDIDO, idPedido, e.getMessage());
            throw new ExcelBusinessException(
                    String.format("Error al generar informe Excel del pedido %d: %s", idPedido, e.getMessage()), e);
        }
    }

    private Pedido obtenerPedido(Integer idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ExcelBusinessException(ExcelConstants.ERROR_PEDIDO_NO_ENCONTRADO));
    }

    private void validarPedidoEntregado(Pedido pedido, Integer idPedido) {
        if (pedido.getEstadoPedido() != EstadoPedidoEnum.ENT) {
            log.warn(ExcelConstants.LOG_PEDIDO_ESTADO_INVALIDO, idPedido, pedido.getEstadoPedido());
            throw new ExcelBusinessException(ExcelConstants.ERROR_PEDIDO_NO_ENTREGADO);
        }
    }

    private List<Solicitud> obtenerSolicitudesPagadasPedido(Pedido pedido, Integer idPedido) {
        List<Solicitud> solicitudes = solicitudRepository.findByPedidoWithProductos(pedido).stream()
                .filter(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.PGD)
                .collect(Collectors.toList());

        if (solicitudes.isEmpty()) {
            log.warn(ExcelConstants.LOG_PEDIDO_SIN_SOLICITUDES, idPedido);
            throw new ExcelBusinessException(ExcelConstants.ERROR_PEDIDO_SIN_SOLICITUDES);
        }

        return solicitudes;
    }

    private int construirInformePedido(Sheet sheet, Pedido pedido,
            List<Solicitud> solicitudes,
            Map<String, CellStyle> estilos) {
        int rowNum = 0;

        // Título
        rowHelper.crearFilaTitulo(sheet, rowNum++,
                ExcelConstants.TITULO_INFORME_PEDIDO + pedido.getIdPedido(),
                ExcelConstants.HEADERS_INFORME_PEDIDO.length,
                estilos.get("titulo"));
        rowNum++;

        // Información del pedido
        String infoPedido = ExcelConstants.PREFIJO_INFO_PEDIDO + pedido.getIdPedido() +
                ExcelConstants.PREFIJO_INFO_ESTADO + ExcelConstants.ESTADO_ENTREGADO +
                ExcelConstants.PREFIJO_INFO_FECHA + pedido.getFechaCreado().format(FECHA_FORMATTER);
        rowHelper.crearFilaInfo(sheet, rowNum++, infoPedido,
                ExcelConstants.HEADERS_INFORME_PEDIDO.length,
                estilos.get("normal"));
        rowNum++;

        // Headers
        rowHelper.crearFilaHeaders(sheet, rowNum++,
                ExcelConstants.HEADERS_INFORME_PEDIDO,
                estilos.get("header"));

        // Consolidar y agregar datos
        List<ProductoConsolidado> productosConsolidados = consolidacionHelper.consolidarProductos(solicitudes);
        BigDecimal totalGeneral = agregarFilasProductosConsolidados(sheet, productosConsolidados,
                rowNum, estilos);
        rowNum += productosConsolidados.size();

        // Total
        rowNum++;
        rowHelper.crearFilaTotal(sheet, rowNum, 4, 5, totalGeneral, estilos.get("total"));

        return rowNum;
    }

    private BigDecimal agregarFilasProductosConsolidados(Sheet sheet,
            List<ProductoConsolidado> productos,
            int rowNum,
            Map<String, CellStyle> estilos) {
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (ProductoConsolidado producto : productos) {
            Row row = sheet.createRow(rowNum++);
            BigDecimal precioTotal = producto.calcularPrecioTotal();
            totalGeneral = totalGeneral.add(precioTotal);

            rowHelper.crearCeldaNumerica(row, 0,
                    producto.getCodigoProducto(),
                    estilos.get("normal"));
            rowHelper.crearCeldaTexto(row, 1,
                    producto.getFechaPrimera().format(FECHA_FORMATTER),
                    estilos.get("fecha"));
            rowHelper.crearCeldaTexto(row, 2,
                    producto.getNombreProducto(),
                    estilos.get("normal"));
            rowHelper.crearCeldaNumerica(row, 3,
                    producto.getCantidad(),
                    estilos.get("normal"));
            rowHelper.crearCeldaMoneda(row, 4,
                    producto.getPrecio(),
                    estilos.get("moneda"));
            rowHelper.crearCeldaMoneda(row, 5,
                    precioTotal,
                    estilos.get("moneda"));
        }

        return totalGeneral;
    }

    private void configurarAnchoColumnasPedido(Sheet sheet) {
        sheet.setColumnWidth(0, ExcelConstants.ANCHO_COLUMNA_CODIGO);
        sheet.setColumnWidth(1, ExcelConstants.ANCHO_COLUMNA_FECHA);
        sheet.setColumnWidth(2, ExcelConstants.ANCHO_COLUMNA_PRODUCTO);
        sheet.setColumnWidth(3, ExcelConstants.ANCHO_COLUMNA_CANTIDAD);
        sheet.setColumnWidth(4, ExcelConstants.ANCHO_COLUMNA_PRECIO);
        sheet.setColumnWidth(5, ExcelConstants.ANCHO_COLUMNA_PRECIO);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Crea un mapa con todos los estilos necesarios para el informe.
     */
    private Map<String, CellStyle> crearMapaEstilos(Workbook workbook) {
        Map<String, CellStyle> estilos = new HashMap<>();
        estilos.put("titulo", styleHelper.crearEstiloTitulo(workbook));
        estilos.put("header", styleHelper.crearEstiloHeader(workbook));
        estilos.put("normal", styleHelper.crearEstiloNormal(workbook));
        estilos.put("moneda", styleHelper.crearEstiloMoneda(workbook));
        estilos.put("fecha", styleHelper.crearEstiloFecha(workbook));
        estilos.put("total", styleHelper.crearEstiloTotal(workbook));
        return estilos;
    }
}
