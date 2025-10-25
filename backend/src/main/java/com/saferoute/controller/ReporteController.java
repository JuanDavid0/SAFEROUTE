package com.saferoute.controller;

import com.saferoute.dto.reporte.*;
import com.saferoute.service.IReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para reportes y estadísticas
 * Endpoints protegidos para Administradores
 */
@Slf4j
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReporteController {

    private final IReporteService reporteService;

    /**
     * GET /reportes/ingresos
     * Genera reporte de ingresos en un rango de fechas
     * 
     * @param fechaInicio Fecha inicial (formato: yyyy-MM-dd)
     * @param fechaFin    Fecha final (formato: yyyy-MM-dd)
     * @param agrupacion  "MENSUAL" o "ANUAL" (opcional, default: MENSUAL)
     * @return Reporte con ingresos, costos y ganancias
     */
    @GetMapping("/ingresos")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerReporteIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false, defaultValue = "MENSUAL") String agrupacion) {

        try {
            log.info("📊 GET /reportes/ingresos - Rango: {} a {}, Agrupación: {}",
                    fechaInicio, fechaFin, agrupacion);

            // Validar fechas
            if (fechaInicio.isAfter(fechaFin)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "La fecha de inicio no puede ser posterior a la fecha final"));
            }

            // Validar agrupación
            if (!agrupacion.equals("MENSUAL") && !agrupacion.equals("ANUAL")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "La agrupación debe ser 'MENSUAL' o 'ANUAL'"));
            }

            ReporteIngresosDTO reporte = reporteService.generarReporteIngresos(
                    fechaInicio, fechaFin, agrupacion);

            return ResponseEntity.ok(reporte);

        } catch (Exception e) {
            log.error("❌ Error generando reporte de ingresos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al generar el reporte: " + e.getMessage()));
        }
    }

    /**
     * GET /reportes/productos-mas-vendidos
     * Obtiene los productos más vendidos
     * 
     * @param limite Número máximo de productos (opcional, default: 10)
     * @return Lista de productos ordenados por cantidad vendida
     */
    @GetMapping("/productos-mas-vendidos")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerProductosMasVendidos(
            @RequestParam(required = false, defaultValue = "10") Integer limite) {

        try {
            log.info("📦 GET /reportes/productos-mas-vendidos - Límite: {}", limite);

            if (limite <= 0 || limite > 100) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "El límite debe estar entre 1 y 100"));
            }

            List<ProductoVendidoDTO> productos = reporteService
                    .obtenerProductosMasVendidos(limite);

            return ResponseEntity.ok(productos);

        } catch (Exception e) {
            log.error("❌ Error obteniendo productos más vendidos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al obtener productos: " + e.getMessage()));
        }
    }

    /**
     * GET /reportes/productos-mayor-ganancia
     * Obtiene los productos con mayor ganancia
     * 
     * @param limite Número máximo de productos (opcional, default: 10)
     * @return Lista de productos ordenados por ganancia
     */
    @GetMapping("/productos-mayor-ganancia")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerProductosMayorGanancia(
            @RequestParam(required = false, defaultValue = "10") Integer limite) {

        try {
            log.info("💰 GET /reportes/productos-mayor-ganancia - Límite: {}", limite);

            if (limite <= 0 || limite > 100) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "El límite debe estar entre 1 y 100"));
            }

            List<ProductoVendidoDTO> productos = reporteService
                    .obtenerProductosMayorGanancia(limite);

            return ResponseEntity.ok(productos);

        } catch (Exception e) {
            log.error("❌ Error obteniendo productos con mayor ganancia", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al obtener productos: " + e.getMessage()));
        }
    }

    /**
     * GET /reportes/clientes-frecuentes
     * Obtiene los clientes más frecuentes
     * 
     * @param limite Número máximo de clientes (opcional, default: 20)
     * @return Lista de clientes ordenados por número de solicitudes
     */
    @GetMapping("/clientes-frecuentes")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerClientesFrecuentes(
            @RequestParam(required = false, defaultValue = "20") Integer limite) {

        try {
            log.info("👥 GET /reportes/clientes-frecuentes - Límite: {}", limite);

            if (limite <= 0 || limite > 100) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "El límite debe estar entre 1 y 100"));
            }

            List<ClienteFrecuenteDTO> clientes = reporteService
                    .obtenerClientesFrecuentes(limite);

            return ResponseEntity.ok(clientes);

        } catch (Exception e) {
            log.error("❌ Error obteniendo clientes frecuentes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al obtener clientes: " + e.getMessage()));
        }
    }

    /**
     * GET /reportes/pedidos-en-curso
     * Obtiene los pedidos que están en curso (ACT, RTA, ADU)
     * 
     * @return Lista de pedidos en curso con su progreso
     */
    @GetMapping("/pedidos-en-curso")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerPedidosEnCurso() {

        try {
            log.info("🚚 GET /reportes/pedidos-en-curso");

            List<PedidoEnCursoDTO> pedidos = reporteService.obtenerPedidosEnCurso();

            return ResponseEntity.ok(pedidos);

        } catch (Exception e) {
            log.error("❌ Error obteniendo pedidos en curso", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al obtener pedidos: " + e.getMessage()));
        }
    }

    /**
     * GET /reportes/resumen
     * Genera un resumen general de estadísticas
     * Dashboard principal
     * 
     * @return Resumen con totales de pedidos, solicitudes, ingresos, etc.
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerResumenEstadisticas() {

        try {
            log.info("📈 GET /reportes/resumen");

            ResumenEstadisticasDTO resumen = reporteService.obtenerResumenEstadisticas();

            return ResponseEntity.ok(resumen);

        } catch (Exception e) {
            log.error("❌ Error generando resumen de estadísticas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al generar resumen: " + e.getMessage()));
        }
    }

    /**
     * GET /reportes/health
     * Endpoint de verificación del servicio de reportes
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ReporteService");
        health.put("timestamp", LocalDate.now());
        return ResponseEntity.ok(health);
    }
}
