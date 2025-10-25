package com.saferoute.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para el reporte de ingresos
 * Contiene datos agregados por periodo (mensual/anual)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteIngresosDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String agrupacion; // "MENSUAL" o "ANUAL"

    private BigDecimal totalIngresos;
    private BigDecimal totalCostos;
    private BigDecimal gananciaNeta;

    private Integer totalSolicitudesPagadas;

    // Datos para gráficos (por periodo)
    private List<DatosPeriodo> datosPorPeriodo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatosPeriodo {
        private String periodo; // "2025-01" o "2025"
        private BigDecimal ingresos;
        private BigDecimal costos;
        private BigDecimal ganancia;
        private Integer cantidadSolicitudes;
    }
}
