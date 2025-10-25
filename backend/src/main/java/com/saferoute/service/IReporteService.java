package com.saferoute.service;

import com.saferoute.dto.reporte.*;

import java.time.LocalDate;
import java.util.List;

public interface IReporteService {

    ReporteIngresosDTO generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin, String agrupacion);

    List<ProductoVendidoDTO> obtenerProductosMasVendidos(Integer limite);

    List<ProductoVendidoDTO> obtenerProductosMayorGanancia(Integer limite);

    List<ClienteFrecuenteDTO> obtenerClientesFrecuentes(Integer limite);

    List<PedidoEnCursoDTO> obtenerPedidosEnCurso();

    ResumenEstadisticasDTO obtenerResumenEstadisticas();
}
