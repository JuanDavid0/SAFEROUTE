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

    @GetMapping("/ingresos")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerReporteIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false, defaultValue = "TRIMESTRAL") String agrupacion) {

        log.info("📊 GET /reportes/ingresos - Rango: {} a {}, Agrupación: {}",
                fechaInicio, fechaFin, agrupacion);

        // Validar fechas
        if (fechaInicio.isAfter(fechaFin)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La fecha de inicio no puede ser posterior a la fecha final"));
        }

        // Validar agrupación
        if (!agrupacion.equals("TRIMESTRAL") && !agrupacion.equals("ANUAL")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La agrupación debe ser 'TRIMESTRAL' o 'ANUAL'"));
        }

        ReporteIngresosDTO reporte = reporteService.generarReporteIngresos(
                fechaInicio, fechaFin, agrupacion);

        return ResponseEntity.ok(reporte);
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

        log.info("📊 GET /reportes/productos-mas-vendidos - Límite: {}", limite);

        if (limite <= 0 || limite > 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "El límite debe estar entre 1 y 100"));
        }

        List<ProductoVendidoDTO> productos = reporteService
                .obtenerProductosMasVendidos(limite);

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos-mayor-ganancia")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerProductosMayorGanancia(
            @RequestParam(required = false, defaultValue = "10") Integer limite) {

        log.info("📊 GET /reportes/productos-mayor-ganancia - Límite: {}", limite);

        if (limite <= 0 || limite > 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "El límite debe estar entre 1 y 100"));
        }

        List<ProductoVendidoDTO> productos = reporteService
                .obtenerProductosMayorGanancia(limite);

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/clientes-frecuentes")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerClientesFrecuentes(
            @RequestParam(required = false, defaultValue = "20") Integer limite) {

        log.info("📊 GET /reportes/clientes-frecuentes - Límite: {}", limite);

        if (limite <= 0 || limite > 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "El límite debe estar entre 1 y 100"));
        }

        List<ClienteFrecuenteDTO> clientes = reporteService
                .obtenerClientesFrecuentes(limite);

        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/pedidos-en-curso")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerPedidosEnCurso() {

        log.info("📊 GET /reportes/pedidos-en-curso");

        List<PedidoEnCursoDTO> pedidos = reporteService.obtenerPedidosEnCurso();

        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<?> obtenerResumenEstadisticas() {

        log.info("📊 GET /reportes/resumen");

        ResumenEstadisticasDTO resumen = reporteService.obtenerResumenEstadisticas();

        return ResponseEntity.ok(resumen);
    }

}
