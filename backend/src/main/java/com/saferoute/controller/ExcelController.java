package com.saferoute.controller;

import com.saferoute.service.interfaces.IExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 📊 CONTROLADOR DE EXPORTACIÓN A EXCEL
 * 
 * Endpoints para generar y descargar informes contables en formato Excel
 * (.xlsx).
 * Informes disponibles:
 * - Compras de clientes
 * - Productos consolidados de pedidos entregados
 * 
 * RF007: Exportación a Excel
 * 
 * @author SafeRoute Team
 * @version 2.0
 * @since 2025-01-24
 */
@Slf4j
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExcelController {

    private final IExcelService excelService;

    /**
     * 📄 GET /excel/cliente/{id}
     * Genera un archivo Excel con las compras de un cliente específico
     * 
     * Estructura del Excel:
     * - Título: Informe de Compras - {Nombre Cliente}
     * - Columnas: Fecha | Producto | Cantidad | Precio Unitario | Precio Total
     * - Solo incluye solicitudes pagadas (PGD)
     * - Ordenado por fecha descendente
     * - Incluye fila de total
     * 
     * @param idCliente ID del cliente
     * @return Archivo Excel listo para descargar
     */
    @GetMapping("/cliente/{id}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<byte[]> generarInformeCliente(@PathVariable("id") Integer idCliente) {
        log.info("📥 Solicitud para generar Excel de compras del cliente #{}", idCliente);

        try {
            ByteArrayOutputStream excel = excelService.generarInformeCliente(idCliente);

            String filename = String.format("Compras_Cliente_%d_%s.xlsx",
                    idCliente,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excel.size());

            log.info("✅ Excel de compras del cliente #{} generado exitosamente - {} bytes",
                    idCliente, excel.size());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel.toByteArray());

        } catch (RuntimeException e) {
            log.error("❌ Error de negocio al generar Excel del cliente #{}: {}", idCliente, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Error técnico al generar Excel del cliente #{}: {}", idCliente, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📊 GET /excel/pedido/{id}
     * Genera un archivo Excel con los productos consolidados de un pedido entregado
     * 
     * Estructura del Excel:
     * - Título: Informe de Pedido #{idPedido}
     * - Columnas: Código Producto | Fecha | Nombre Producto | Cantidad | Precio
     * Unitario | Precio Total
     * - Solo incluye pedidos con estado ENT (Entregado)
     * - Solo incluye solicitudes pagadas (PGD)
     * - Productos consolidados (suma de cantidades del mismo producto)
     * - Ordenado por código de producto
     * - Incluye fila de total
     * 
     * @param idPedido ID del pedido
     * @return Archivo Excel listo para descargar
     */
    @GetMapping("/pedido/{id}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<byte[]> generarInformePedido(@PathVariable("id") Integer idPedido) {
        log.info("📥 Solicitud para generar Excel del pedido #{}", idPedido);

        try {
            ByteArrayOutputStream excel = excelService.generarInformePedidoConsolidado(idPedido);

            String filename = String.format("Informe_Pedido_%d_%s.xlsx",
                    idPedido,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excel.size());

            log.info("✅ Excel del pedido #{} generado exitosamente - {} bytes", idPedido, excel.size());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel.toByteArray());

        } catch (RuntimeException e) {
            log.error("❌ Error de negocio al generar Excel del pedido #{}: {}", idPedido, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Error técnico al generar Excel del pedido #{}: {}", idPedido, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🏥 GET /excel/health
     * Health check del servicio de Excel
     * 
     * @return Confirmación de que el servicio está operativo
     */
    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('SAD', 'ADM', 'CLI')")
    public ResponseEntity<String> healthCheck() {
        log.info("🏥 Health check del servicio Excel");
        return ResponseEntity.ok("📊 Servicio de exportación a Excel operativo");
    }
}
