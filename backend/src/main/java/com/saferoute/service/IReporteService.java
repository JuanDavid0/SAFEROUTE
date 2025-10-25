package com.saferoute.service;

import com.saferoute.dto.reporte.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para generar reportes y estadísticas
 */
public interface IReporteService {

    /**
     * Genera reporte de ingresos en un rango de fechas
     * 
     * @param fechaInicio Fecha inicial
     * @param fechaFin    Fecha final
     * @param agrupacion  "MENSUAL" o "ANUAL"
     * @return Reporte con ingresos, costos y ganancias
     */
    ReporteIngresosDTO generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin, String agrupacion);

    /**
     * Obtiene los productos más vendidos
     * 
     * @param limite Número máximo de productos a retornar (default 10)
     * @return Lista de productos ordenados por cantidad vendida
     */
    List<ProductoVendidoDTO> obtenerProductosMasVendidos(Integer limite);

    /**
     * Obtiene los productos con mayor ganancia
     * 
     * @param limite Número máximo de productos a retornar (default 10)
     * @return Lista de productos ordenados por ganancia
     */
    List<ProductoVendidoDTO> obtenerProductosMayorGanancia(Integer limite);

    /**
     * Obtiene los clientes más frecuentes
     * 
     * @param limite Número máximo de clientes a retornar (default 20)
     * @return Lista de clientes ordenados por número de solicitudes
     */
    List<ClienteFrecuenteDTO> obtenerClientesFrecuentes(Integer limite);

    /**
     * Obtiene los pedidos que están en curso (ACT, RTA, ADU, DST)
     * 
     * @return Lista de pedidos en curso con su progreso
     */
    List<PedidoEnCursoDTO> obtenerPedidosEnCurso();

    /**
     * Genera un resumen general de estadísticas
     * 
     * @return Resumen con totales de pedidos, solicitudes, ingresos, etc.
     */
    ResumenEstadisticasDTO obtenerResumenEstadisticas();
}
