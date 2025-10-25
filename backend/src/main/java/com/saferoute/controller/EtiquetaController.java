package com.saferoute.controller;

import com.saferoute.dto.EtiquetasResponseDTO;
import com.saferoute.service.interfaces.IEtiquetaService;
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
 * 🏷️ CONTROLADOR DE ETIQUETAS DE PEDIDO
 * 
 * Endpoints para generar etiquetas de entrega de pedidos.
 * Solo para pedidos en estado ENTREGADO (ENT).
 * 
 * Endpoints disponibles:
 * - GET /etiquetas/pedido/{id}/json - Obtiene etiquetas en formato JSON
 * - GET /etiquetas/pedido/{id}/pdf - Descarga etiquetas en formato PDF
 * - GET /etiquetas/health - Health check del servicio
 * 
 * Acceso:
 * - Requiere rol SAD o ADM
 * 
 * @author SafeRoute Team
 * @version 1.0
 * @since 2025-10-24
 */
@Slf4j
@RestController
@RequestMapping("/etiquetas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EtiquetaController {

    private final IEtiquetaService etiquetaService;

    /**
     * 📋 GET /etiquetas/pedido/{id}/json
     * Genera las etiquetas de un pedido en formato JSON
     * 
     * Formato de respuesta:
     * {
     * "idPedido": 123,
     * "fechaEntrega": "24/10/2025",
     * "etiquetas": [
     * {
     * "idSolicitud": 456,
     * "nombreCliente": "Juan Pérez",
     * "direccion": "Calle 123 #45-67",
     * "telefono": "3001234567",
     * "numeroEtiqueta": 1,
     * "totalEtiquetas": 3
     * },
     * ...
     * ],
     * "totalEtiquetas": 3
     * }
     * 
     * @param idPedido ID del pedido entregado
     * @return ResponseEntity con el DTO de etiquetas
     */
    @GetMapping("/pedido/{id}/json")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<EtiquetasResponseDTO> generarEtiquetasJSON(@PathVariable("id") Integer idPedido) {
        log.info("📥 Solicitud para generar etiquetas JSON del pedido #{}", idPedido);

        try {
            EtiquetasResponseDTO etiquetas = etiquetaService.generarEtiquetasJSON(idPedido);

            log.info("✅ Etiquetas JSON del pedido #{} generadas exitosamente - {} etiquetas",
                    idPedido, etiquetas.getTotalEtiquetas());

            return ResponseEntity.ok(etiquetas);

        } catch (RuntimeException e) {
            log.error("❌ Error al generar etiquetas JSON del pedido {}: {}", idPedido, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Error interno al generar etiquetas JSON del pedido {}: {}", idPedido, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📄 GET /etiquetas/pedido/{id}/pdf
     * Genera un PDF con las etiquetas del pedido para impresión
     * 
     * Formato del PDF:
     * - Diseño en cuadrícula (2 etiquetas por fila)
     * - Cada etiqueta contiene:
     * * Logo "SAFE ROUTE"
     * * Cliente: {Nombre Completo}
     * * Dirección: {Dirección}
     * * Contacto: {Teléfono}
     * * Número de etiqueta (ej: 1/5)
     * - Bordes para facilitar el corte
     * - Listo para imprimir
     * 
     * @param idPedido ID del pedido entregado
     * @return Archivo PDF listo para descargar
     */
    @GetMapping("/pedido/{id}/pdf")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<byte[]> generarEtiquetasPDF(@PathVariable("id") Integer idPedido) {
        log.info("📥 Solicitud para generar PDF de etiquetas del pedido #{}", idPedido);

        try {
            ByteArrayOutputStream pdf = etiquetaService.generarEtiquetasPDF(idPedido);

            String filename = String.format("Etiquetas_Pedido_%d_%s.pdf",
                    idPedido,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdf.size());

            log.info("✅ PDF de etiquetas del pedido #{} generado exitosamente - {} bytes",
                    idPedido, pdf.size());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf.toByteArray());

        } catch (RuntimeException e) {
            log.error("❌ Error al generar PDF de etiquetas del pedido {}: {}", idPedido, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Error interno al generar PDF de etiquetas del pedido {}: {}", idPedido, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🏥 GET /etiquetas/health
     * Verifica el estado del servicio de etiquetas
     * 
     * @return Mensaje de estado del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("🏷️ Servicio de Etiquetas - ACTIVO");
    }
}
